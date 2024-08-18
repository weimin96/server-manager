package io.github.weimin96.manager.server.web.reactive;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.services.InstanceRegistry;
import io.github.weimin96.manager.server.web.ServerController;
import io.github.weimin96.manager.server.web.HttpHeaderFilter;
import io.github.weimin96.manager.server.web.InstanceWebProxy;
import io.github.weimin96.manager.server.web.client.InstanceWebClient;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

/**
 * @author panwm
 * @since 2024/8/4 23:42
 */
@ServerController
public class InstancesProxyController {

    private static final String INSTANCE_MAPPED_PATH = "/api/instances/{instanceId}/actuator/**";

    private static final String APPLICATION_MAPPED_PATH = "/api/applications/{applicationName}/actuator/**";

    private final PathMatcher pathMatcher = new AntPathMatcher();

    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    private final InstanceRegistry registry;

    private final InstanceWebProxy instanceWebProxy;

    private final String contextPath;

    private final HttpHeaderFilter httpHeadersFilter;

    public InstancesProxyController(String contextPath, Set<String> ignoredHeaders, InstanceRegistry registry,
                                    InstanceWebClient instanceWebClient) {
        this.contextPath = contextPath;
        this.registry = registry;
        this.httpHeadersFilter = new HttpHeaderFilter(ignoredHeaders);
        this.instanceWebProxy = new InstanceWebProxy(instanceWebClient);
    }

    @RequestMapping(path = INSTANCE_MAPPED_PATH, method = { RequestMethod.GET, RequestMethod.HEAD, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS })
    public Mono<Void> endpointProxy(@PathVariable("instanceId") String instanceId, ServerHttpRequest request,
                                    ServerHttpResponse response) {
        InstanceWebProxy.ForwardRequest fwdRequest = createForwardRequest(request, request.getBody(),
                this.contextPath + INSTANCE_MAPPED_PATH);

        return this.instanceWebProxy.forward(this.registry.getInstance(InstanceId.of(instanceId)), fwdRequest,
                (clientResponse) -> {
                    response.setStatusCode(clientResponse.statusCode());
                    response.getHeaders()
                            .addAll(this.httpHeadersFilter.filterHeaders(clientResponse.headers().asHttpHeaders()));
                    return response.writeAndFlushWith(clientResponse.body(BodyExtractors.toDataBuffers()).window(1));
                });
    }

    @ResponseBody
    @RequestMapping(path = APPLICATION_MAPPED_PATH, method = { RequestMethod.GET, RequestMethod.HEAD,
            RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS })
    public Flux<InstanceWebProxy.InstanceResponse> endpointProxy(
            @PathVariable("applicationName") String applicationName, ServerHttpRequest request) {

        Flux<DataBuffer> cachedBody = request.getBody().map((b) -> {
            int readableByteCount = b.readableByteCount();
            DataBuffer dataBuffer = this.bufferFactory.allocateBuffer(readableByteCount);
            dataBuffer.write(b.asByteBuffer());
            DataBufferUtils.release(b);
            return dataBuffer;
        }).cache();

        InstanceWebProxy.ForwardRequest fwdRequest = createForwardRequest(request, cachedBody,
                this.contextPath + APPLICATION_MAPPED_PATH);

        return this.instanceWebProxy.forward(this.registry.getInstances(applicationName), fwdRequest);
    }

    private InstanceWebProxy.ForwardRequest createForwardRequest(ServerHttpRequest request, Flux<DataBuffer> cachedBody,
                                                                 String pathPattern) {
        String localPath = this.getLocalPath(pathPattern, request);
        URI uri = UriComponentsBuilder.fromPath(localPath).query(request.getURI().getRawQuery()).build(true).toUri();
        return InstanceWebProxy.ForwardRequest.builder().uri(uri).method(request.getMethod())
                .headers(this.httpHeadersFilter.filterHeaders(request.getHeaders()))
                .body(BodyInserters.fromDataBuffers(cachedBody)).build();
    }

    private String getLocalPath(String pathPattern, ServerHttpRequest request) {
        String pathWithinApplication = request.getPath().pathWithinApplication().value();
        return this.pathMatcher.extractPathWithinPattern(pathPattern, pathWithinApplication);
    }
}
