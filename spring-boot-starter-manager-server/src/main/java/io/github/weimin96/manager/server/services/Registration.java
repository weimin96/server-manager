package io.github.weimin96.manager.server.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 注册实例入参
 *
 * @author panwm
 * @since 2024/8/3 10:20
 */
@Data
@ToString(exclude = "metadata")
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class Registration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final String managementUrl;

    private final String healthUrl;

    private final String serviceUrl;

    private final String source;

    private final Map<String, String> metadata;

    @JsonCreator
    public Registration(
            @JsonProperty("name") String name,
            @JsonProperty("managementUrl") String managementUrl,
            @JsonProperty("healthUrl") String healthUrl,
            @JsonProperty("serviceUrl") String serviceUrl,
            @JsonProperty("source") String source,
            @JsonProperty("metadata") @lombok.Singular("metadata") Map<String, String> metadata) {
        this.name = name;
        this.managementUrl = managementUrl;
        this.healthUrl = healthUrl;
        this.serviceUrl = serviceUrl;
        this.source = source;
        this.metadata = new LinkedHashMap<>();
        this.metadata.putAll(metadata);
    }

    public static Registration.Builder create(String name, String healthUrl) {
        return builder().name(name).healthUrl(healthUrl);
    }

    public static Registration.Builder copyOf(Registration registration) {
        return registration.toBuilder();
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(this.metadata);
    }
}
