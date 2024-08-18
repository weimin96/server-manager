package io.github.weimin96.manager.server.web;

import io.github.weimin96.manager.server.domain.entity.Application;
import io.github.weimin96.manager.server.services.ApplicationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author panwm
 * @since 2024/8/2 23:10
 */
@Slf4j
@ServerController
@ResponseBody
public class ApplicationsController {

    private static final ServerSentEvent<?> PING = ServerSentEvent.builder().comment("ping").build();

    private static final Flux<ServerSentEvent<?>> PING_FLUX = Flux.interval(Duration.ZERO, Duration.ofSeconds(10L))
            .map((tick) -> PING);

    private final ApplicationRegistry registry;

    public ApplicationsController(ApplicationRegistry registry) {
        this.registry = registry;
    }

    @GetMapping(path = "/api/applications", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Application> applications() {
        return registry.getApplications();
    }

    @GetMapping(path = "/api/applications/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Application>> application(@PathVariable("name") String name) {
        return registry.getApplication(name).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/api/applications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Application>> applicationsStream() {
        return registry.getApplicationStream().map((application) -> ServerSentEvent.builder(application).build())
                .mergeWith(ping());
    }

    @DeleteMapping(path = "/api/applications/{name}")
    public Mono<ResponseEntity<Void>> unregister(@PathVariable("name") String name) {
        log.debug("Unregister application with name '{}'", name);
        return registry.deregister(name).collectList().map((deregistered) -> !deregistered.isEmpty()
                ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build());
    }

    @SuppressWarnings("unchecked")
    private static <T> Flux<ServerSentEvent<T>> ping() {
        return (Flux<ServerSentEvent<T>>) (Flux) PING_FLUX;
    }

}
