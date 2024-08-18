package io.github.weimin96.manager.server.domain.events;

import io.github.weimin96.manager.server.domain.value.InstanceId;

import java.io.Serializable;
import java.time.Instant;

/**
 * 实例事件
 * @author panwm
 * @since 2024/8/3 10:55
 */
@lombok.Data
@lombok.ToString
public abstract class InstanceEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final InstanceId instance;

    private final long version;

    private final Instant timestamp;

    private final String type;

    protected InstanceEvent(InstanceId instance, long version, Instant timestamp, String type) {
        this.instance = instance;
        this.version = version;
        this.timestamp = timestamp;
        this.type = type;
    }


}
