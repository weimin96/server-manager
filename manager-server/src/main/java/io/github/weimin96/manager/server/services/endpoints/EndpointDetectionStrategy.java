package io.github.weimin96.manager.server.services.endpoints;

import io.github.weimin96.manager.server.domain.entity.Instance;
import io.github.weimin96.manager.server.domain.value.Endpoints;
import reactor.core.publisher.Mono;

/**
 * 端点检测策略
 * @author panwm
 * @since 2024/8/5 0:07
 */
public interface EndpointDetectionStrategy {

    Mono<Endpoints> detectEndpoints(Instance instance);

}
