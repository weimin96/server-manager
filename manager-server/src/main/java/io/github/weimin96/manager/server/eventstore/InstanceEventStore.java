package io.github.weimin96.manager.server.eventstore;

import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import io.github.weimin96.manager.server.domain.value.InstanceId;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

/**
 * @author panwm
 * @since 2024/8/3 11:25
 */
@Slf4j
public class InstanceEventStore extends InstanceEventPublisher {

    /**
     * 排序 时间+版本 最新的在前面
     */
    private static final Comparator<InstanceEvent> byTimestampAndIdAndVersion = comparing(InstanceEvent::getTimestamp)
            .thenComparing(InstanceEvent::getInstance).thenComparing(InstanceEvent::getVersion);

    /**
     * 最大事件容量
     */
    private final int maxLogSizePerAggregate;

    /**
     * 事件日志
     */
    private final ConcurrentMap<InstanceId, List<InstanceEvent>> eventLog;

    public InstanceEventStore() {
        this(100);
    }

    public InstanceEventStore(int maxLogSizePerAggregate) {
        this(maxLogSizePerAggregate, new ConcurrentHashMap<>());
    }

    public InstanceEventStore(int maxLogSizePerAggregate, ConcurrentMap<InstanceId, List<InstanceEvent>> eventLog) {
        this.maxLogSizePerAggregate = maxLogSizePerAggregate;
        this.eventLog = eventLog;
    }

    public Flux<InstanceEvent> findAll() {
        // 创建一个延迟创建的 Flux。只有在这个 Flux 被订阅的时候才会创建数据源。
        return Flux.defer(() -> Flux.fromIterable(eventLog.values()).flatMapIterable(Function.identity())
                .sort(byTimestampAndIdAndVersion));
    }

    public Flux<InstanceEvent> find(InstanceId id) {
        return Flux.defer(() -> Flux.fromIterable(eventLog.getOrDefault(id, Collections.emptyList())));
    }

    public Mono<Void> append(List<InstanceEvent> events) {
        // 添加事件 发布
        return Mono.fromRunnable(() -> {
            while (true) {
                if (doAppend(events)) {
                    return;
                }
            }
        }).then(Mono.fromRunnable(() -> this.publish(events)));
    }

    protected boolean doAppend(List<InstanceEvent> events) {
        // 如果事件列表为空，true完成
        if (events.isEmpty()) {
            return true;
        }

        InstanceId id = events.get(0).getInstance();
        // 事件列表中的事件不属于同一个实例
        if (!events.stream().allMatch((event) -> event.getInstance().equals(id))) {
            throw new IllegalArgumentException("事件实例ID不一致");
        }
        // 不存在则初始化事件列表
        List<InstanceEvent> oldEvents = eventLog.computeIfAbsent(id,
                (key) -> new ArrayList<>(maxLogSizePerAggregate + 1));
        // 获取原先最新版本
        long lastVersion = getLastVersion(oldEvents);
        // 原先最新版本大于等于当前最新版本
        if (lastVersion >= events.get(0).getVersion()) {
            throw new IllegalArgumentException("版本号错误，当前版本【" + events.get(0).getVersion() + "】必须大于旧版本【" + lastVersion + "】-" + events.get(0).getInstance());
        }

        List<InstanceEvent> newEvents = new ArrayList<>(oldEvents);
        newEvents.addAll(events);

        if (newEvents.size() > maxLogSizePerAggregate) {
            // 压缩事件列表
            compact(newEvents);
        }

        if (eventLog.replace(id, oldEvents, newEvents)) {
            log.debug("存储事件成功 {}", events);
            return true;
        }

        log.debug("存储事件失败 {} ", events);
        return false;
    }

    private void compact(List<InstanceEvent> events) {
        // 返回版本号较大的事件
        BinaryOperator<InstanceEvent> latestEvent = (e1, e2) -> (e1.getVersion() > e2.getVersion()) ? e1 : e2;
        // 按事件类型分组，返回版本号较大的事件
        Map<Class<?>, Optional<InstanceEvent>> latestPerType = events.stream()
                .collect(groupingBy(InstanceEvent::getClass, reducing(latestEvent)));
        // 移除那些不是最新版本的事件
        events.removeIf((e) -> !Objects.equals(e, latestPerType.get(e.getClass()).orElse(null)));
    }


    /**
     * 获取最新版本
     * @param events 事件列表
     * @return 最新版本
     */
    public static long getLastVersion(List<InstanceEvent> events) {
        return events.isEmpty() ? -1 : events.get(events.size() - 1).getVersion();
    }
}
