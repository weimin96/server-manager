package io.github.weimin96.manager.server.web.servlet;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.services.InstanceRegistry;
import io.github.weimin96.manager.server.web.AdminController;
import io.github.weimin96.manager.server.web.HttpHeaderFilter;
import io.github.weimin96.manager.server.web.InstanceWebProxy;
import io.github.weimin96.manager.server.web.client.InstanceWebClient;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;

/**
 * 代理请求
 * @author panwm
 * @since 2024/8/3 23:56
 */
@AdminController
public class InstancesProxyController {

    private static final String INSTANCE_MAPPED_PATH = "/instances/{instanceId}/actuator/**";

    private static final String APPLICATION_MAPPED_PATH = "/applications/{applicationName}/actuator/**";

    /**
     * 数据缓冲区
     */
    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    private final PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * web代理请求
     */
    private final InstanceWebProxy instanceWebProxy;

    private final HttpHeaderFilter httpHeadersFilter;

    private final InstanceRegistry registry;

    private final String contextPath;

    public InstancesProxyController(String contextPath, Set<String> ignoredHeaders, InstanceRegistry registry,
                                    InstanceWebClient instanceWebClient) {
        this.contextPath = contextPath;
        this.registry = registry;
        this.httpHeadersFilter = new HttpHeaderFilter(ignoredHeaders);
        this.instanceWebProxy = new InstanceWebProxy(instanceWebClient);
    }

    /**
     * 代理请求
     * @param instanceId 实例id
     * @param servletRequest servlet请求
     */
    @ResponseBody
    @RequestMapping(path = INSTANCE_MAPPED_PATH, method = { RequestMethod.GET, RequestMethod.HEAD, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS })
    public void instanceProxy(@PathVariable("instanceId") String instanceId, HttpServletRequest servletRequest) {
        // start async because we will commit from different thread.
        // otherwise incorrect thread local objects (session and security context) will be
        // stored.
        // check for example
        // org.springframework.security.web.context.HttpSessionSecurityContextRepository.SaveToSessionRequestWrapper.startAsync()
        AsyncContext asyncContext = servletRequest.startAsync();
        asyncContext.setTimeout(-1); // no timeout because instanceWebProxy will handle it
        // for us
        try {
            ServletServerHttpRequest request = new ServletServerHttpRequest(
                    (HttpServletRequest) asyncContext.getRequest());
            Flux<DataBuffer> requestBody = DataBufferUtils.readInputStream(request::getBody, this.bufferFactory, 4096);
            InstanceWebProxy.ForwardRequest fwdRequest = createForwardRequest(request, requestBody,
                    this.contextPath + INSTANCE_MAPPED_PATH);

            this.instanceWebProxy
                    .forward(this.registry.getInstance(InstanceId.of(instanceId)), fwdRequest, (clientResponse) -> {
                        ServerHttpResponse response = new ServletServerHttpResponse(
                                (HttpServletResponse) asyncContext.getResponse());
                        response.setStatusCode(clientResponse.statusCode());
                        response.getHeaders()
                                .addAll(this.httpHeadersFilter.filterHeaders(clientResponse.headers().asHttpHeaders()));
                        try {
                            OutputStream responseBody = response.getBody();
                            response.flush();
                            return clientResponse.body(BodyExtractors.toDataBuffers()).window(1)
                                    .concatMap((body) -> writeAndFlush(body, responseBody)).then();
                        }
                        catch (IOException ex) {
                            return Mono.error(ex);
                        }
                    })
                    // We need to explicitly block so the headers are recieved and written
                    // before any async dispatch otherwise the FrameworkServlet will add
                    // wrong
                    // Allow header for OPTIONS request
                    .block();
        }
        finally {
            asyncContext.complete();
        }
    }

    @ResponseBody
    @RequestMapping(path = APPLICATION_MAPPED_PATH, method = { RequestMethod.GET, RequestMethod.HEAD,
            RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS })
    public Flux<InstanceWebProxy.InstanceResponse> endpointProxy(
            @PathVariable("applicationName") String applicationName, HttpServletRequest servletRequest) {

        ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        Flux<DataBuffer> cachedBody = DataBufferUtils.readInputStream(request::getBody, this.bufferFactory, 4096)
                .cache();

        InstanceWebProxy.ForwardRequest fwdRequest = createForwardRequest(request, cachedBody,
                this.contextPath + APPLICATION_MAPPED_PATH);
        return this.instanceWebProxy.forward(this.registry.getInstances(applicationName), fwdRequest);
    }

    private InstanceWebProxy.ForwardRequest createForwardRequest(ServletServerHttpRequest request,
                                                                 Flux<DataBuffer> body, String pathPattern) {
        String endpointLocalPath = this.getLocalPath(pathPattern, request);
        URI uri = UriComponentsBuilder.fromPath(endpointLocalPath).query(request.getURI().getRawQuery()).build(true)
                .toUri();

        return InstanceWebProxy.ForwardRequest.builder().uri(uri).method(request.getMethod())
                .headers(this.httpHeadersFilter.filterHeaders(request.getHeaders()))
                .body(BodyInserters.fromDataBuffers(body)).build();
    }

    private String getLocalPath(String pathPattern, ServletServerHttpRequest request) {
        String pathWithinApplication = UriComponentsBuilder.fromPath(request.getServletRequest()
                .getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString()).toUriString();
        return this.pathMatcher.extractPathWithinPattern(pathPattern, pathWithinApplication);
    }

    private Mono<Void> writeAndFlush(Flux<DataBuffer> body, OutputStream responseBody) {
        return DataBufferUtils.write(body, responseBody).map(DataBufferUtils::release).then(Mono.create((sink) -> {
            try {
                responseBody.flush();
                sink.success();
            }
            catch (IOException ex) {
                sink.error(ex);
            }
        }));
    }

}
