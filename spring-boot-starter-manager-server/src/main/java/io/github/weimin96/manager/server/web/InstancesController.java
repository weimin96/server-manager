package io.github.weimin96.manager.server.web;

import io.github.weimin96.manager.server.domain.entity.Instance;
import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.eventstore.InstanceEventStore;
import io.github.weimin96.manager.server.services.InstanceRegistry;
import io.github.weimin96.manager.server.services.Registration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * @author panwm
 * @since 2024/8/3 10:16
 */
@Slf4j
@ResponseBody
@ServerController
public class InstancesController {

    private final InstanceRegistry registry;

    private final InstanceEventStore eventStore;

    /**
     * 服务器向客户端发送事件
     */
    private static final ServerSentEvent<?> PING = ServerSentEvent.builder().comment("ping").build();

    /**
     * 创建了一个每隔10秒发送一个值的序列
     */
    private static final Flux<ServerSentEvent<?>> PING_FLUX = Flux.interval(Duration.ZERO, Duration.ofSeconds(10L))
            .map((tick) -> PING);

    public InstancesController(InstanceRegistry registry, InstanceEventStore eventStore) {
        this.registry = registry;
        this.eventStore = eventStore;
    }

    /**
     * 注册实例
     * @param registration registration
     * @param builder builder
     * @return Map
     */
    @PostMapping(path = "/api/instances", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, InstanceId>>> register(@RequestBody Registration registration,
                                                                  UriComponentsBuilder builder) {
        // 创建注册实例
        Registration withSource = Registration.copyOf(registration).source("http-api").build();
        log.debug("注册实例 {}", withSource);
        return registry.register(withSource).map((id) -> {
            URI location = builder.replacePath("/instances/{id}").buildAndExpand(id).toUri();
            return ResponseEntity.created(location).body(Collections.singletonMap("id", id));
        });
    }

    /**
     * 通过名称获取实例列表
     * @param name 实例名称
     * @return 实例
     */
    @GetMapping(path = "/api/instances", produces = MediaType.APPLICATION_JSON_VALUE, params = "name")
    public Flux<Instance> instances(@RequestParam("name") String name) {
        return registry.getInstances(name).filter(Instance::isRegistered);
    }

    /**
     * 获取实例列表
     * @return 实例列表
     */
    @GetMapping(path = "/api/instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Instance> instances() {
        log.debug("Deliver all registered instances");
        return registry.getInstances().filter(Instance::isRegistered);
    }

    /**
     * 通过id获取实例
     * @param id 应用id
     * @return 实例
     */
    @GetMapping(path = "/api/instances/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Instance>> instance(@PathVariable String id) {
        log.debug("Deliver registered instance with ID '{}'", id);
        return registry.getInstance(InstanceId.of(id)).filter(Instance::isRegistered).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * 注销实例
     * @param id 实例id
     * @return ResponseEntity
     */
    @DeleteMapping(path = "/api/instances/{id}")
    public Mono<ResponseEntity<Void>> unregister(@PathVariable String id) {
        log.debug("Unregister instance with ID '{}'", id);
        return registry.deregister(InstanceId.of(id)).map((v) -> ResponseEntity.noContent().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/api/instances/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<InstanceEvent> events() {
        return eventStore.findAll();
    }

    @GetMapping(path = "/api/instances/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<InstanceEvent>> eventStream() {
        return Flux.from(eventStore).map((event) -> ServerSentEvent.builder(event).build()).mergeWith(ping());
    }

    /**
     * 存活检测
     * @param id 应用id
     * @return ServerSentEvent
     */
    @GetMapping(path = "/api/instances/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Instance>> instanceStream(@PathVariable String id) {
        return Flux.from(eventStore).filter((event) -> event.getInstance().equals(InstanceId.of(id)))
                .flatMap((event) -> registry.getInstance(event.getInstance()))
                .map((event) -> ServerSentEvent.builder(event).build()).mergeWith(ping());
    }

    @SuppressWarnings("unchecked")
    private static <T> Flux<ServerSentEvent<T>> ping() {
        return (Flux<ServerSentEvent<T>>) (Flux) PING_FLUX;
    }

}
