package io.github.weimin96.manager.server.domain.entity;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import io.github.weimin96.manager.server.eventstore.InstanceEventStore;
import io.github.weimin96.manager.server.eventstore.OptimisticLockingException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 事件实现类
 *
 * @author panwm
 * @since 2024/8/3 11:21
 */
@Slf4j
public class EventInstanceRepository implements InstanceRepository {

    private final ConcurrentMap<InstanceId, Instance> snapshots = new ConcurrentHashMap<>();

    private final Set<InstanceId> oudatedSnapshots = ConcurrentHashMap.newKeySet();

    /**
     * 事件存储
     */
    private final InstanceEventStore eventStore;

    private Disposable subscription;

    private final Retry retryOptimisticLockException = Retry.max(10)
            .doBeforeRetry((s) -> log.debug("Retrying after OptimisticLockingException", s.failure()))
            .filter(OptimisticLockingException.class::isInstance);

    public EventInstanceRepository(InstanceEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public Mono<Instance> saveEventStore(Instance instance) {
        return this.eventStore.append(instance.getUnsavedEvents()).then(Mono.just(instance.clearUnsavedEvents()));
    }

    @Override
    public Mono<Instance> save(Instance instance) {
        return this.saveEventStore(instance).doOnError(OptimisticLockingException.class,
                (e) -> this.oudatedSnapshots.add(instance.getId()));
    }

    @Override
    public Flux<Instance> findAll() {
        return Mono.fromSupplier(this.snapshots::values).flatMapIterable(Function.identity());
    }

    @Override
    public Mono<Instance> find(InstanceId id) {
        return Mono.defer(() -> {
            if (!this.oudatedSnapshots.contains(id)) {
                return Mono.justOrEmpty(this.snapshots.get(id));
            }
            else {
                return rehydrateSnapshot(id).doOnSuccess((v) -> this.oudatedSnapshots.remove(v.getId()));
            }
        });
    }

    @Override
    public Flux<Instance> findByName(String name) {
        return findAll().filter((a) -> a.isRegistered() && name.equals(a.getRegistration().getName()));
    }

    @Override
    public Mono<Instance> compute(InstanceId id, BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction) {
        // 查找对应实例
        return this.find(id)
                // 异步 展平映射
                .flatMap((application) -> remappingFunction.apply(id, application))
                // 如果没有找到实例，则处理一个空实例
                .switchIfEmpty(Mono.defer(() -> remappingFunction.apply(id, null)))
                // 保存实例
                .flatMap(this::save)
                .retryWhen(this.retryOptimisticLockException);
    }

    @Override
    public Mono<Instance> computeIfPresent(InstanceId id,
                                           BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction) {
        return this.find(id).flatMap((application) -> remappingFunction.apply(id, application)).flatMap(this::save)
                .retryWhen(this.retryOptimisticLockException);
    }

    public void start() {
        this.subscription = this.eventStore.findAll().concatWith(this.eventStore).subscribe(this::updateSnapshot);
    }

    public void stop() {
        if (this.subscription != null) {
            this.subscription.dispose();
            this.subscription = null;
        }
    }

    public Mono<Instance> findEventStore(InstanceId id) {
        return this.eventStore.find(id).collectList().filter((e) -> !e.isEmpty())
                .map((e) -> Instance.create(id).apply(e));
    }

    protected Mono<Instance> rehydrateSnapshot(InstanceId id) {
        return this.findEventStore(id).map((instance) -> this.snapshots.compute(id, (key, snapshot) -> {
            // check if the loaded version hasn't been already outdated by a snapshot
            if (snapshot == null || instance.getVersion() >= snapshot.getVersion()) {
                return instance;
            }
            else {
                return snapshot;
            }
        }));
    }

    protected void updateSnapshot(InstanceEvent event) {
        try {
            this.snapshots.compute(event.getInstance(), (key, old) -> {
                Instance instance = (old != null) ? old : Instance.create(key);
                if (event.getVersion() > instance.getVersion()) {
                    return instance.apply(event);
                }
                return instance;
            });
        }
        catch (Exception ex) {
            log.warn("Error while updating the snapshot with event {}", event, ex);
        }
    }

}
