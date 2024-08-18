package io.github.weimin96.manager.server.web.client;

import io.github.weimin96.manager.server.domain.entity.Instance;
import io.github.weimin96.manager.server.web.client.exception.ResolveInstanceException;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 客户端请求工具类
 *
 * @author panwm
 * @since 2024/8/4 23:22
 */
public class InstanceWebClient {

    public static final String ATTRIBUTE_INSTANCE = "instance";

    private final WebClient webClient;

    protected InstanceWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public WebClient instance(Mono<Instance> instance) {
        return this.webClient.mutate()
                .filters((filters) -> filters.add(0, setInstance(instance)))
                .build();
    }

    public WebClient instance(Instance instance) {
        return this.instance(Mono.justOrEmpty(instance));
    }

    public static InstanceWebClient.Builder builder() {
        return new InstanceWebClient.Builder();
    }

    public static InstanceWebClient.Builder builder(WebClient.Builder webClient) {
        return new InstanceWebClient.Builder(webClient);
    }

    private static ExchangeFilterFunction setInstance(Mono<Instance> instance) {
        return (request, next) -> instance
                .map((i) -> ClientRequest.from(request).attribute(ATTRIBUTE_INSTANCE, i).build())
                .switchIfEmpty(Mono.error(() -> new ResolveInstanceException("Could not resolve Instance")))
                .flatMap(next::exchange);
    }

    private static ExchangeFilterFunction toExchangeFilterFunction(InstanceExchangeFilterFunction filter) {
        return (request, next) -> request.attribute(ATTRIBUTE_INSTANCE).filter(Instance.class::isInstance)
                .map(Instance.class::cast).map((instance) -> filter.filter(instance, request, next))
                .orElseGet(() -> next.exchange(request));
    }

    public static class Builder {

        private List<InstanceExchangeFilterFunction> filters = new ArrayList<>();

        private WebClient.Builder webClientBuilder;

        public Builder() {
            this(WebClient.builder());
        }

        public Builder(WebClient.Builder webClientBuilder) {
            this.webClientBuilder = webClientBuilder;
        }

        protected Builder(Builder other) {
            this.filters = new ArrayList<>(other.filters);
            this.webClientBuilder = other.webClientBuilder.clone();
        }

        public Builder filter(InstanceExchangeFilterFunction filter) {
            this.filters.add(filter);
            return this;
        }

        public Builder filters(Consumer<List<InstanceExchangeFilterFunction>> filtersConsumer) {
            filtersConsumer.accept(this.filters);
            return this;
        }

        public Builder webClient(WebClient.Builder builder) {
            this.webClientBuilder = builder;
            return this;
        }

        public Builder clone() {
            return new Builder(this);
        }

        public InstanceWebClient build() {
            this.filters.stream().map(InstanceWebClient::toExchangeFilterFunction)
                    .forEach(this.webClientBuilder::filter);
            return new InstanceWebClient(this.webClientBuilder.build());
        }
    }
}
