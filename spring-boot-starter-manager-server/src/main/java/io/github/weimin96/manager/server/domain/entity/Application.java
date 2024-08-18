package io.github.weimin96.manager.server.domain.entity;

import io.github.weimin96.manager.server.domain.value.BuildVersion;
import io.github.weimin96.manager.server.domain.value.StatusInfo;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author panwm
 * @since 2024/8/4 13:25
 */
@lombok.Data
public class Application {

    private final String name;

    private final BuildVersion buildVersion;

    private final String status;

    private final Instant statusTimestamp;

    private final List<Instance> instances;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Application(String name, BuildVersion buildVersion, String status,
                        Instant statusTimestamp, List<Instance> instances) {
        Assert.notNull(name, "'name' must not be null");
        this.name = name;
        this.buildVersion = buildVersion;
        this.status = (status != null) ? status : StatusInfo.STATUS_UNKNOWN;
        this.statusTimestamp = (statusTimestamp != null) ? statusTimestamp : Instant.now();
        if (instances.isEmpty()) {
            this.instances = Collections.emptyList();
        } else {
            this.instances = new ArrayList<>(instances);

        }
    }

    public static Application.Builder create(String name) {
        return builder().name(name);
    }

}
