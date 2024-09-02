package io.github.weimin96.manager.server.services;

import io.github.weimin96.manager.server.domain.entity.Instance;
import io.github.weimin96.manager.server.domain.entity.InstanceRepository;
import io.github.weimin96.manager.server.domain.value.Endpoint;
import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.domain.value.StatusInfo;
import io.github.weimin96.manager.server.web.client.InstanceWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import static java.util.Collections.emptyMap;

/**
 * 更新查询healthUrl的所有或单个应用程序的状态。
 *
 * @author panwm
 * @since 2024/8/4 23:53
 */
@Slf4j
@RequiredArgsConstructor
public class StatusUpdater {

    /**
     * 用于获取泛型类型信息
     */
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };

    private final InstanceRepository repository;

    private final InstanceWebClient instanceWebClient;

    private final ApiMediaTypeHandler apiMediaTypeHandler;

    private Duration timeout = Duration.ofSeconds(10);

    public StatusUpdater timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 更新所有应用程序的状态。
     *
     * @param id 实例id
     * @return 更新状态
     */
    public Mono<Void> updateStatus(InstanceId id) {
        return this.repository.computeIfPresent(id, (key, instance) -> this.doUpdateStatus(instance)).then();
    }

    /**
     * 更新单个应用程序的状态。
     *
     * @param instance 实例
     * @return 实例
     */
    protected Mono<Instance> doUpdateStatus(Instance instance) {
        if (!instance.isRegistered()) {
            return Mono.empty();
        }

        log.debug("Update status for {}", instance);
        return this.instanceWebClient.instance(instance)
                .get()
                .uri(Endpoint.HEALTH)
                .exchangeToMono(this::convertStatusInfo)
                .log(log.getName(), Level.FINEST)
                .timeout(getTimeoutWithMargin())
                .doOnError((ex) -> logError(instance, ex))
                .onErrorResume(this::handleError).map(instance::withStatusInfo);
    }

    /*
     * return a timeout less than the given one to prevent backdrops in concurrent get
     * request. This prevents flakyness of health checks.
     */
    private Duration getTimeoutWithMargin() {
        return this.timeout.minusSeconds(1).abs();
    }

    protected Mono<StatusInfo> convertStatusInfo(ClientResponse response) {
        boolean hasCompatibleContentType = response.headers().contentType().filter(
                        (mt) -> mt.isCompatibleWith(MediaType.APPLICATION_JSON) || this.apiMediaTypeHandler.isApiMediaType(mt))
                .isPresent();

        StatusInfo statusInfoFromStatus = this.getStatusInfoFromStatus(response.statusCode(), emptyMap());
        if (hasCompatibleContentType) {
            return response.bodyToMono(RESPONSE_TYPE).map((body) -> {
                if (body.get("status") instanceof String) {
                    return StatusInfo.from(body);
                }
                return getStatusInfoFromStatus(response.statusCode(), body);
            }).defaultIfEmpty(statusInfoFromStatus);
        }
        return response.releaseBody().then(Mono.just(statusInfoFromStatus));
    }

    @SuppressWarnings("unchecked")
    protected StatusInfo getStatusInfoFromStatus(HttpStatus httpStatus, Map<String, ?> body) {
        if (httpStatus.is2xxSuccessful()) {
            return StatusInfo.ofUp();
        }
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", httpStatus.value());
        details.put("error", httpStatus.getReasonPhrase());
        if (body.get("details") instanceof Map) {
            details.putAll((Map<? extends String, ?>) body.get("details"));
        } else {
            details.putAll(body);
        }
        return StatusInfo.ofDown(details);
    }

    protected Mono<StatusInfo> handleError(Throwable ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("message", ex.getMessage());
        details.put("exception", ex.getClass().getName());
        return Mono.just(StatusInfo.ofOffline(details));
    }

    protected void logError(Instance instance, Throwable ex) {
        if (instance.getStatusInfo().isOffline()) {
            log.debug("更新服务状态失败 {}", instance, ex);
        } else {
            log.info("更新服务状态失败 {}", instance);
            log.debug(ex.getMessage(), ex);
        }
    }
}
