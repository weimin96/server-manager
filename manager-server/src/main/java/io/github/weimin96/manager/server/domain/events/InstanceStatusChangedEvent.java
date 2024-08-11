package io.github.weimin96.manager.server.domain.events;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.domain.value.StatusInfo;

import java.time.Instant;

/**
 * 实例状态改变事件
 * @author panwm
 * @since 2024/8/3 22:57
 */
@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceStatusChangedEvent extends InstanceEvent {

    public static final String TYPE = "STATUS_CHANGED";

    private static final long serialVersionUID = 1L;

    private final StatusInfo statusInfo;

    public InstanceStatusChangedEvent(InstanceId instance, long version, StatusInfo statusInfo) {
        this(instance, version, Instant.now(), statusInfo);
    }

    public InstanceStatusChangedEvent(InstanceId instance, long version, Instant timestamp, StatusInfo statusInfo) {
        super(instance, version, timestamp, TYPE);
        this.statusInfo = statusInfo;
    }

    public StatusInfo getStatusInfo() {
        return statusInfo;
    }

}
