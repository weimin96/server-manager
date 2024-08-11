package io.github.weimin96.manager.server.domain.value;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * actuator endpoint信息
 *
 * @author panwm
 * @since 2024/8/3 22:25
 */
@lombok.Data
public class Info implements Serializable {

    private static final Info EMPTY = new Info(Collections.emptyMap());

    private final Map<String, Object> values;

    private Info(Map<String, Object> values) {
        if (values.isEmpty()) {
            this.values = Collections.emptyMap();
        } else {
            this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        }
    }

    public static Info from(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return empty();
        }
        return new Info(values);
    }

    public static Info empty() {
        return EMPTY;
    }

    public Map<String, Object> getValues() {
        return this.values;
    }
}
