package io.github.weimin96.manager.server.services;

import io.github.weimin96.manager.server.domain.entity.Instance;
import io.github.weimin96.manager.server.domain.entity.InstanceRepository;
import io.github.weimin96.manager.server.domain.value.InstanceId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 实例注册表
 * @author panwm
 * @since 2024/8/2 23:29
 */
public class InstanceRegistry {

    private final InstanceRepository repository;

    private final InstanceIdGenerator generator;;

    public InstanceRegistry(InstanceRepository repository, InstanceIdGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    /**
     * 注册实例
     * @param registration 注册信息
     * @return 实例id
     */
    public Mono<InstanceId> register(Registration registration) {
        // 创建实例id
        InstanceId id = generator.generateId(registration);
        // 注册实例
        return repository.compute(id, (key, instance) -> {
            if (instance == null) {
                instance = Instance.create(key);
            }
            return Mono.just(instance.register(registration));
        }).map(Instance::getId);
    }

    /**
     * 获取所有实例
     * @return 实例列表
     */
    public Flux<Instance> getInstances() {
        return repository.findAll();
    }

    /**
     * 获取指定应用的实例
     * @param name 应用名称
     * @return 实例列表
     */
    public Flux<Instance> getInstances(String name) {
        return repository.findByName(name);
    }

    /**
     * 获取指定实例
     * @param id 实例id
     * @return 实例
     */
    public Mono<Instance> getInstance(InstanceId id) {
        return repository.find(id);
    }

    /**
     * 注销实例
     * @param id 实例id
     * @return 实例id
     */
    public Mono<InstanceId> deregister(InstanceId id) {
        return repository.computeIfPresent(id, (key, instance) -> Mono.just(instance.deregister()))
                .map(Instance::getId);
    }

}
