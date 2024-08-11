package io.github.weimin96.manager.server.domain.events;

import io.github.weimin96.manager.server.domain.value.InstanceId;

import java.time.Instant;

/**
 * 实例注销事件
 * @author panwm
 * @since 2024/8/3 22:22
 */
@lombok.Setter
@lombok.Getter
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceDeregisteredEvent extends InstanceEvent{

    public static final String TYPE = "DEREGISTERED";

    private static final long serialVersionUID = 1L;

    public InstanceDeregisteredEvent(InstanceId instance, long version) {
        this(instance, version, Instant.now());
    }

    public InstanceDeregisteredEvent(InstanceId instance, long version, Instant timestamp) {
        super(instance, version, timestamp, TYPE);
    }
}
