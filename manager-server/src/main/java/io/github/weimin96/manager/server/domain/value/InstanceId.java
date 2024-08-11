package io.github.weimin96.manager.server.domain.value;

import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * 实例id
 * @author panwm
 * @since 2024/8/2 23:40
 */
@Data
public class InstanceId  implements Serializable, Comparable<InstanceId> {

    private final String value;

    public static InstanceId of(String value) {
        return new InstanceId(value);
    }

    public InstanceId(String value) {
        Assert.hasText(value, "'value' must have text");
        this.value = value;
    }

    @Override
    public int compareTo(InstanceId o) {
        return this.value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
