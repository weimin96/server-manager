package io.github.weimin96.manager.server.domain.events;

import io.github.weimin96.manager.server.domain.value.Endpoints;
import io.github.weimin96.manager.server.domain.value.InstanceId;

import java.time.Instant;

/**
 * 检测所有实例的端点事件
 *
 * @author panwm
 * @since 2024/8/3 22:59
 */
@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceEndpointsDetectedEvent extends InstanceEvent {

    public static final String TYPE = "ENDPOINTS_DETECTED";

    private static final long serialVersionUID = 1L;

    private final Endpoints endpoints;

    public InstanceEndpointsDetectedEvent(InstanceId instance, long version, Endpoints endpoints) {
        this(instance, version, Instant.now(), endpoints);
    }

    public InstanceEndpointsDetectedEvent(InstanceId instance, long version, Instant timestamp, Endpoints endpoints) {
        super(instance, version, timestamp, TYPE);
        this.endpoints = endpoints;
    }

}
