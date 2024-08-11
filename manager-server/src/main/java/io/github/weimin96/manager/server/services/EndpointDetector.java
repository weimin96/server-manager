package io.github.weimin96.manager.server.services;

import io.github.weimin96.manager.server.domain.entity.Instance;
import io.github.weimin96.manager.server.domain.entity.InstanceRepository;
import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.services.endpoints.EndpointDetectionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

/**
 *
 * @author panwm
 * @since 2024/8/5 0:06
 */
@Slf4j
public class EndpointDetector {

    private final InstanceRepository repository;

    private final EndpointDetectionStrategy strategy;

    public EndpointDetector(InstanceRepository repository, EndpointDetectionStrategy strategy) {
        this.repository = repository;
        this.strategy = strategy;
    }

    public Mono<Void> detectEndpoints(InstanceId id) {
        return repository.computeIfPresent(id, (key, instance) -> this.doDetectEndpoints(instance)).then();
    }

    private Mono<Instance> doDetectEndpoints(Instance instance) {
        if (!StringUtils.hasText(instance.getRegistration().getManagementUrl()) || instance.getStatusInfo().isOffline()
                || instance.getStatusInfo().isUnknown()) {
            return Mono.empty();
        }
        log.debug("Detect endpoints for {}", instance);
        return strategy.detectEndpoints(instance).map(instance::withEndpoints);
    }
}
