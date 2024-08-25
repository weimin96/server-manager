package io.github.weimin96.manager.server.domain.events;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.services.Registration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 实例注册更新事件
 * @author panwm
 * @since 2024/8/3 22:20
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InstanceRegistrationUpdatedEvent extends InstanceEvent {

    public static final String TYPE = "REGISTRATION_UPDATED";

    private static final long serialVersionUID = 1L;

    private final Registration registration;

    public InstanceRegistrationUpdatedEvent(InstanceId instance, long version, Registration registration) {
        this(instance, version, Instant.now(), registration);
    }

    public InstanceRegistrationUpdatedEvent(InstanceId instance, long version, Instant timestamp,
                                            Registration registration) {
        super(instance, version, timestamp, TYPE );
        this.registration = registration;
    }
}
