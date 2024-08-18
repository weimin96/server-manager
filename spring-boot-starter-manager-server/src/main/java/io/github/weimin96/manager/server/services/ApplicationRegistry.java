package io.github.weimin96.manager.server.services;

import io.github.weimin96.manager.server.domain.entity.Application;
import io.github.weimin96.manager.server.domain.entity.Instance;
import io.github.weimin96.manager.server.domain.value.BuildVersion;
import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.domain.value.StatusInfo;
import io.github.weimin96.manager.server.eventstore.InstanceEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * 应用注册
 *
 * @author panwm
 * @since 2024/8/2 23:11
 */
public class ApplicationRegistry {

    private final InstanceRegistry instanceRegistry;

    private final InstanceEventPublisher instanceEventPublisher;

    public ApplicationRegistry(InstanceRegistry instanceRegistry, InstanceEventPublisher instanceEventPublisher) {
        this.instanceRegistry = instanceRegistry;
        this.instanceEventPublisher = instanceEventPublisher;
    }

    /**
     * 获取所有应用
     *
     * @return 所有应用
     */
    public Flux<Application> getApplications() {
        return this.instanceRegistry.getInstances().filter(Instance::isRegistered).groupBy((instance) -> instance.getRegistration().getName()).flatMap((grouped) -> toApplication(grouped.key(), grouped), Integer.MAX_VALUE);
    }

    /**
     * 获取指定应用
     *
     * @param name 应用名称
     * @return 指定应用
     */
    public Mono<Application> getApplication(String name) {
        return this.toApplication(name, this.instanceRegistry.getInstances(name).filter(Instance::isRegistered)).filter((a) -> !a.getInstances().isEmpty());
    }

    public Flux<Application> getApplicationStream() {
        return Flux.from(this.instanceEventPublisher).flatMap((event) -> this.instanceRegistry.getInstance(event.getInstance())).map(this::getApplicationForInstance).flatMap((group) -> toApplication(group.getT1(), group.getT2()));
    }

    public Flux<InstanceId> deregister(String name) {
        return this.instanceRegistry.getInstances(name).flatMap((instance) -> this.instanceRegistry.deregister(instance.getId()));
    }

    /**
     * 获取指定实例对应的应用
     * @param instance 实例
     * @return 应用
     */
    protected Tuple2<String, Flux<Instance>> getApplicationForInstance(Instance instance) {
        String name = instance.getRegistration().getName();
        return Tuples.of(name, this.instanceRegistry.getInstances(name).filter(Instance::isRegistered));
    }

    /**
     * 将实例列表转换为应用
     * @param name 应用名称
     * @param instances 实例列表
     * @return
     */
    protected Mono<Application> toApplication(String name, Flux<Instance> instances) {
        return instances.collectList().map((instanceList) -> {
            Tuple2<String, Instant> status = getStatus(instanceList);
            return Application.create(name).instances(instanceList).buildVersion(getBuildVersion(instanceList)).status(status.getT1()).statusTimestamp(status.getT2()).build();
        });
    }

    protected BuildVersion getBuildVersion(List<Instance> instances) {
        List<BuildVersion> versions = instances.stream().map(Instance::getBuildVersion).filter(Objects::nonNull).distinct().sorted().collect(toList());
        if (versions.isEmpty()) {
            return null;
        } else if (versions.size() == 1) {
            return versions.get(0);
        } else {
            return BuildVersion.valueOf(versions.get(0) + " ... " + versions.get(versions.size() - 1));
        }
    }

    protected Tuple2<String, Instant> getStatus(List<Instance> instances) {
        // TODO: Correct is just a second readmodel for groups
        Map<String, Instant> statusWithTime = instances.stream().collect(toMap((instance) -> instance.getStatusInfo().getStatus(), Instance::getStatusTimestamp, this::getMax));
        if (statusWithTime.size() == 1) {
            Map.Entry<String, Instant> e = statusWithTime.entrySet().iterator().next();
            return Tuples.of(e.getKey(), e.getValue());
        }

        if (statusWithTime.containsKey(StatusInfo.STATUS_UP)) {
            Instant oldestNonUp = statusWithTime.entrySet().stream().filter((e) -> !StatusInfo.STATUS_UP.equals(e.getKey())).map(Map.Entry::getValue).min(naturalOrder()).orElse(Instant.EPOCH);
            Instant latest = getMax(oldestNonUp, statusWithTime.getOrDefault(StatusInfo.STATUS_UP, Instant.EPOCH));
            return Tuples.of(StatusInfo.STATUS_RESTRICTED, latest);
        }

        return statusWithTime.entrySet().stream().min(Map.Entry.comparingByKey(StatusInfo.severity())).map((e) -> Tuples.of(e.getKey(), e.getValue())).orElse(Tuples.of(StatusInfo.STATUS_UNKNOWN, Instant.EPOCH));
    }

    /**
     * 获取最大时间
     * @param t1 t1
     * @param t2 t2
     * @return 最大时间
     */
    protected Instant getMax(Instant t1, Instant t2) {
        return (t1.compareTo(t2) >= 0) ? t1 : t2;
    }

}
