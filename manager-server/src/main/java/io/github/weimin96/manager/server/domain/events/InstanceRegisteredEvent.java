package io.github.weimin96.manager.server.domain.events;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.services.Registration;

import java.time.Instant;

/**
 * 实例注册事件
 * @author panwm
 * @since 2024/8/3 10:58
 */
@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode(callSuper = true)
// 继承属性一致
public class InstanceRegisteredEvent extends InstanceEvent {

    public static final String TYPE = "REGISTERED";

    private final Registration registration;

    public InstanceRegisteredEvent(InstanceId instance, long version, Registration registration) {
        this(instance, version, Instant.now(), registration);
    }

    public InstanceRegisteredEvent(InstanceId instance, long version, Instant timestamp, Registration registration) {
        super(instance, version, timestamp, TYPE);
        this.registration = registration;
    }
}
