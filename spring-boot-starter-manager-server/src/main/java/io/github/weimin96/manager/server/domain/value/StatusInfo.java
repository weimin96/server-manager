package io.github.weimin96.manager.server.domain.value;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * 实例状态
 * @author panwm
 * @since 2024/8/3 22:35
 */
@lombok.Data
public class StatusInfo implements Serializable {

    public static final String STATUS_UNKNOWN = "UNKNOWN";

    public static final String STATUS_OUT_OF_SERVICE = "OUT_OF_SERVICE";

    public static final String STATUS_UP = "UP";

    public static final String STATUS_DOWN = "DOWN";

    public static final String STATUS_OFFLINE = "OFFLINE";

    public static final String STATUS_RESTRICTED = "RESTRICTED";

    private static final List<String> STATUS_ORDER = asList(STATUS_DOWN, STATUS_OUT_OF_SERVICE, STATUS_OFFLINE,
            STATUS_UNKNOWN, STATUS_RESTRICTED, STATUS_UP);

    private final String status;

    private final Map<String, Object> details;

    private StatusInfo(String status, Map<String, ?> details) {
        Assert.hasText(status, "'status' must not be empty.");
        this.status = status.toUpperCase();
        this.details = (details != null) ? new HashMap<>(details) : Collections.emptyMap();
    }

    public static StatusInfo valueOf(String statusCode, Map<String, ?> details) {
        return new StatusInfo(statusCode, details);
    }

    public static StatusInfo valueOf(String statusCode) {
        return valueOf(statusCode, null);
    }

    public static StatusInfo ofUnknown() {
        return valueOf(STATUS_UNKNOWN, null);
    }

    public static StatusInfo ofUp() {
        return ofUp(null);
    }

    public static StatusInfo ofDown() {
        return ofDown(null);
    }

    public static StatusInfo ofOffline() {
        return ofOffline(null);
    }

    public static StatusInfo ofUp(Map<String, Object> details) {
        return valueOf(STATUS_UP, details);
    }

    public static StatusInfo ofDown(Map<String, Object> details) {
        return valueOf(STATUS_DOWN, details);
    }

    public static StatusInfo ofOffline(Map<String, Object> details) {
        return valueOf(STATUS_OFFLINE, details);
    }

    public Map<String, Object> getDetails() {
        return Collections.unmodifiableMap(details);
    }

    public boolean isUp() {
        return STATUS_UP.equals(status);
    }

    public boolean isOffline() {
        return STATUS_OFFLINE.equals(status);
    }

    public boolean isDown() {
        return STATUS_DOWN.equals(status);
    }

    public boolean isUnknown() {
        return STATUS_UNKNOWN.equals(status);
    }

    public static Comparator<String> severity() {
        return Comparator.comparingInt(STATUS_ORDER::indexOf);
    }

    @SuppressWarnings("unchecked")
    public static StatusInfo from(Map<String, ?> body) {
        Map<String, ?> details = Collections.emptyMap();

        if (body.containsKey("details")) {
            details = (Map<String, ?>) body.get("details");
        }
        else if (body.containsKey("components")) {
            details = (Map<String, ?>) body.get("components");
        }

        return StatusInfo.valueOf((String) body.get("status"), details);
    }
}
