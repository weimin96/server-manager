package io.github.weimin96.manager.server.domain.events;

import io.github.weimin96.manager.server.domain.value.Info;
import io.github.weimin96.manager.server.domain.value.InstanceId;

import java.time.Instant;

/**
 * @author panwm
 * @since 2024/8/3 23:03
 */
@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceInfoChangedEvent extends InstanceEvent {

    public static final String TYPE = "INFO_CHANGED";

    private static final long serialVersionUID = 1L;

    private final Info info;

    public InstanceInfoChangedEvent(InstanceId instance, long version, Info info) {
        this(instance, version, Instant.now(), info);
    }

    public InstanceInfoChangedEvent(InstanceId instance, long version, Instant timestamp, Info info) {
        super(instance, version, timestamp, TYPE);
        this.info = info;
    }

}
