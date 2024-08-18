package io.github.weimin96.manager.server.domain.entity;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

/**
 * @author panwm
 * @since 2024/8/2 23:19
 */
public interface InstanceRepository {

    Mono<Instance> save(Instance instance);

    Flux<Instance> findAll();

    Mono<Instance> find(InstanceId id);

    Flux<Instance> findByName(String name);

    Mono<Instance> compute(InstanceId id, BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction);

    Mono<Instance> computeIfPresent(InstanceId id,
                                    BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction);
}
