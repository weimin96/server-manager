package io.github.weimin96.manager.server.services;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * @author panwm
 * @since 2024/8/5 0:00
 */
@Slf4j
public class IntervalCheck {

    private final String name;

    private final Map<InstanceId, Instant> lastChecked = new ConcurrentHashMap<>();

    private final Function<InstanceId, Mono<Void>> checkFn;

    @Getter
    @Setter
    private Duration interval;

    @Setter
    private Duration minRetention;

    private Disposable subscription;

    private Scheduler scheduler;

    public IntervalCheck(String name, Function<InstanceId, Mono<Void>> checkFn) {
        this(name, checkFn, Duration.ofSeconds(10), Duration.ofSeconds(10));
    }

    public IntervalCheck(String name, Function<InstanceId, Mono<Void>> checkFn, Duration interval,
                         Duration minRetention) {
        this.name = name;
        this.checkFn = checkFn;
        this.interval = interval;
        this.minRetention = minRetention;
    }

    public void start() {
        this.scheduler = Schedulers.newSingle(this.name + "-check");
        this.subscription = Flux.interval(this.interval)
                .doOnSubscribe((s) -> log.debug("Scheduled {}-check every {}", this.name, this.interval))
                .log(log.getName(), Level.FINEST).subscribeOn(this.scheduler).concatMap((i) -> this.checkAllInstances())
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .doBeforeRetry((s) -> log.warn("Unexpected error in {}-check", this.name, s.failure())))
                .subscribe(null, (error) -> log.error("Unexpected error in {}-check", name, error));
    }

    public void markAsChecked(InstanceId instanceId) {
        this.lastChecked.put(instanceId, Instant.now());
    }

    protected Mono<Void> checkAllInstances() {
        log.debug("check {} for all instances", this.name);
        Instant expiration = Instant.now().minus(this.minRetention);
        return Flux.fromIterable(this.lastChecked.entrySet()).filter((entry) -> entry.getValue().isBefore(expiration))
                .map(Map.Entry::getKey).flatMap(this.checkFn).then();
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
