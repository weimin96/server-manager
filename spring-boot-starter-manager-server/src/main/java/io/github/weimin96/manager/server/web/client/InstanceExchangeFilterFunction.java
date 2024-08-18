package io.github.weimin96.manager.server.web.client;

import io.github.weimin96.manager.server.domain.entity.Instance;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * 过滤在已注册实例上发出的交换函数
 * @author panwm
 * @since 2024/8/4 23:27
 */
@FunctionalInterface
public interface InstanceExchangeFilterFunction {

    Mono<ClientResponse> filter(Instance instance, ClientRequest request, ExchangeFunction next);

}
