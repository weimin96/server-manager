package io.github.weimin96.manager.server.services;

import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.logging.Level;

/**
 * @author panwm
 * @since 2024/8/4 23:59
 */
public abstract class AbstractEventHandler<T extends InstanceEvent> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Publisher<InstanceEvent> publisher;

    private final Class<T> eventType;

    private Disposable subscription;

    private Scheduler scheduler;

    protected AbstractEventHandler(Publisher<InstanceEvent> publisher, Class<T> eventType) {
        this.publisher = publisher;
        this.eventType = eventType;
    }

    public void start() {
        this.scheduler = this.createScheduler();
        this.subscription = Flux.from(this.publisher).subscribeOn(this.scheduler).log(this.log.getName(), Level.FINEST)
                .doOnSubscribe((s) -> this.log.debug("Subscribed to {} events", this.eventType)).ofType(this.eventType)
                .cast(this.eventType).transform(this::handle)
                .onErrorContinue((throwable, o) -> this.log.warn("Unexpected error", throwable)).subscribe();
    }

    protected abstract Publisher<Void> handle(Flux<T> publisher);

    protected Scheduler createScheduler() {
        return Schedulers.newSingle(this.getClass().getSimpleName());
    }

    public void stop() {
        if (this.subscription != null) {
            this.subscription.dispose();
            this.subscription = null;
        }
        if (this.scheduler != null) {
            this.scheduler.dispose();
            this.scheduler = null;
        }
    }

}
