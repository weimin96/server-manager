package io.github.weimin96.manager.server.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.weimin96.manager.server.domain.entity.Instance;
import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.web.client.InstanceWebClient;
import io.github.weimin96.manager.server.web.client.exception.ResolveEndpointException;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * web代理工具
 * @author panwm
 * @since 2024/8/4 23:20
 */
@Slf4j
public class InstanceWebProxy {

    private static final Instance NULL_INSTANCE = Instance.create(InstanceId.of("null"));

    private final InstanceWebClient instanceWebClient;

    private final ExchangeStrategies strategies = ExchangeStrategies.withDefaults();

    public InstanceWebProxy(InstanceWebClient instanceWebClient) {
        this.instanceWebClient = instanceWebClient;
    }

    /**
     * 转发请求到实例
     * @param instanceMono 实例
     * @param forwardRequest 转发请求
     * @param responseHandler 响应处理器
     * @return 响应
     * @param <V> 响应类型
     */
    public <V> Mono<V> forward(Mono<Instance> instanceMono, ForwardRequest forwardRequest,
                               Function<ClientResponse, Mono<V>> responseHandler) {
        return instanceMono.defaultIfEmpty(NULL_INSTANCE).flatMap((instance) -> {
            if (!instance.equals(NULL_INSTANCE)) {
                return this.forward(instance, forwardRequest, responseHandler);
            }
            else {
                return Mono.defer(() -> responseHandler
                        .apply(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE, this.strategies).build()));
            }
        });
    }

    /**
     * 转发请求到实例
     * @param instances 实例列表
     * @param forwardRequest 转发请求
     * @return 响应列表
     */
    public Flux<InstanceResponse> forward(Flux<Instance> instances, ForwardRequest forwardRequest) {
        return instances.flatMap((instance) -> this.forward(instance, forwardRequest, (clientResponse) -> {
            InstanceResponse.Builder response = InstanceResponse.builder().instanceId(instance.getId())
                    .status(clientResponse.rawStatusCode())
                    .contentType(String.join(", ", clientResponse.headers().header(HttpHeaders.CONTENT_TYPE)));
            return clientResponse.bodyToMono(String.class).map(response::body).defaultIfEmpty(response)
                    .map(InstanceResponse.Builder::build);
        }));
    }

    /**
     * 转发请求到实例
     * @param instance 实例
     * @param forwardRequest 转发请求
     * @param responseHandler 响应处理器
     * @return 响应
     * @param <V> 响应类型
     */
    private <V> Mono<V> forward(Instance instance, ForwardRequest forwardRequest,
                                Function<ClientResponse, Mono<V>> responseHandler) {
        log.trace("Proxy-Request for instance {} with URL '{}'", instance.getId(), forwardRequest.getUri());
        WebClient.RequestBodySpec bodySpec = this.instanceWebClient.instance(instance)
                .method(forwardRequest.getMethod()).uri(forwardRequest.getUri())
                .headers((h) -> h.addAll(forwardRequest.getHeaders()));

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec;
        if (requiresBody(forwardRequest.getMethod())) {
            headersSpec = bodySpec.body(forwardRequest.getBody());
        }

        return headersSpec.exchangeToMono(responseHandler).onErrorResume(ResolveEndpointException.class, (ex) -> {
            log.trace("No Endpoint found for Proxy-Request for instance {} with URL '{}'", instance.getId(),
                    forwardRequest.getUri());
            return responseHandler.apply(ClientResponse.create(HttpStatus.NOT_FOUND, this.strategies).build());
        }).onErrorResume((ex) -> {
            Throwable cause = ex;
            if (ex instanceof WebClientRequestException) {
                cause = ex.getCause();
            }
            if (cause instanceof ReadTimeoutException || cause instanceof TimeoutException) {
                log.trace("Timeout for Proxy-Request for instance {} with URL '{}'", instance.getId(),
                        forwardRequest.getUri());
                return responseHandler
                        .apply(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT, this.strategies).build());
            }
            if (cause instanceof IOException) {
                log.trace("Proxy-Request for instance {} with URL '{}' errored", instance.getId(),
                        forwardRequest.getUri(), cause);
                return responseHandler.apply(ClientResponse.create(HttpStatus.BAD_GATEWAY, this.strategies).build());
            }
            return Mono.error(ex);
        });
    }

    private boolean requiresBody(HttpMethod method) {
        switch (method) {
            case PUT:
            case POST:
            case PATCH:
                return true;
            default:
                return false;
        }
    }

    @lombok.Data
    @lombok.Builder(builderClassName = "Builder")
    public static class InstanceResponse {

        private final InstanceId instanceId;

        private final int status;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private final String body;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private final String contentType;

    }

    @lombok.Data
    @lombok.Builder(builderClassName = "Builder")
    public static class ForwardRequest {

        private final URI uri;

        private final HttpMethod method;

        private final HttpHeaders headers;

        private final BodyInserter<?, ? super ClientHttpRequest> body;

    }
}

