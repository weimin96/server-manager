package io.github.weimin96.manager.server.eventstore;

import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

/**
 * 实例事件发布者
 * @author panwm
 * @since 2024/8/3 11:29
 */
@Slf4j
public class InstanceEventPublisher implements Publisher<InstanceEvent>  {

    private final Flux<InstanceEvent> publishedFlux;

    /**
     * 一个订阅者
     */
    private final Sinks.Many<InstanceEvent> unicast;

    protected InstanceEventPublisher() {
        // 处理背压，它会在缓冲区满时丢弃新的消息，以避免内存溢出
        this.unicast = Sinks.many().unicast().onBackpressureBuffer();
        this.publishedFlux = this.unicast.asFlux().publish().autoConnect(0);
    }

    /**
     * 失败处理 重试
     */
    private final Sinks.EmitFailureHandler emitFailureHandler = (signalType, emitResult) -> emitResult
            .equals(Sinks.EmitResult.FAIL_NON_SERIALIZED);

    /**
     * 发布事件
     * @param events 事件列表
     */
    protected void publish(List<InstanceEvent> events) {
        events.forEach((event) -> {
            log.debug("发布事件 {}", event);
            this.unicast.emitNext(event, emitFailureHandler);
        });
    }

    @Override
    public void subscribe(Subscriber<? super InstanceEvent> subscriber) {
        this.publishedFlux.subscribe(subscriber);
    }
}
