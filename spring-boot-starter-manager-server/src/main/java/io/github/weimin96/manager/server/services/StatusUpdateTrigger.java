package io.github.weimin96.manager.server.services;

import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import io.github.weimin96.manager.server.domain.events.InstanceRegisteredEvent;
import io.github.weimin96.manager.server.domain.events.InstanceRegistrationUpdatedEvent;
import io.github.weimin96.manager.server.domain.value.InstanceId;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author panwm
 * @since 2024/8/4 23:58
 */
public class StatusUpdateTrigger extends AbstractEventHandler<InstanceEvent> {

    private static final Logger log = LoggerFactory.getLogger(StatusUpdateTrigger.class);

    private final StatusUpdater statusUpdater;

    private final IntervalCheck intervalCheck;

    public StatusUpdateTrigger(StatusUpdater statusUpdater, Publisher<InstanceEvent> publisher) {
        super(publisher, InstanceEvent.class);
        this.statusUpdater = statusUpdater;
        this.intervalCheck = new IntervalCheck("status", this::updateStatus);
    }

    @Override
    protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {
        return publisher
                .filter((event) -> event instanceof InstanceRegisteredEvent
                        || event instanceof InstanceRegistrationUpdatedEvent)
                .flatMap((event) -> updateStatus(event.getInstance()));
    }

    protected Mono<Void> updateStatus(InstanceId instanceId) {
        return this.statusUpdater.timeout(this.intervalCheck.getInterval()).updateStatus(instanceId)
                .onErrorResume((e) -> {
                    log.warn("Unexpected error while updating status for {}", instanceId, e);
                    return Mono.empty();
                }).doFinally((s) -> this.intervalCheck.markAsChecked(instanceId));
    }

    @Override
    public void start() {
        super.start();
        this.intervalCheck.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.intervalCheck.stop();
    }

    public void setInterval(Duration updateInterval) {
        this.intervalCheck.setInterval(updateInterval);
    }

    public void setLifetime(Duration statusLifetime) {
        this.intervalCheck.setMinRetention(statusLifetime);
    }

}
