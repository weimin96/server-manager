package io.github.weimin96.manager.server.domain.value;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Scanner;

/**
 * @author panwm
 * @since 2024/8/3 22:42
 */
@Getter
@lombok.Data
public class BuildVersion implements Serializable, Comparable<BuildVersion> {

    private static final String DEFAULT_VERSION = "UNKNOWN";

    private final String value;

    private BuildVersion(String value) {
        if (!StringUtils.hasText(value)) {
            this.value = DEFAULT_VERSION;
        } else {
            this.value = value;
        }
    }

    public static BuildVersion valueOf(String s) {
        return new BuildVersion(s);
    }

    public static BuildVersion from(Map<String, ?> map) {
        if (map.isEmpty()) {
            return null;
        }

        Object build = map.get("build");
        if (build instanceof Map) {
            Object version = ((Map<?, ?>) build).get("version");
            if (version instanceof String) {
                return valueOf((String) version);
            }
        }

        Object version = map.get("build.version");
        if (version instanceof String) {
            return valueOf((String) version);
        }

        version = map.get("version");
        if (version instanceof String) {
            return valueOf((String) version);
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * 如果当前 BuildVersion 大于 other，则返回正整数。
     * 如果当前 BuildVersion 小于 other，则返回负整数。
     * 如果两个 BuildVersion 对象相等，则返回零。
     * @param other the object to be compared.
     * @return int
     */
    @Override
    public int compareTo(BuildVersion other) {
        try (Scanner s1 = new Scanner(this.value); Scanner s2 = new Scanner(other.value)) {
            s1.useDelimiter("[.\\-+]");
            s2.useDelimiter("[.\\-+]");

            while (s1.hasNext() && s2.hasNext()) {
                int c;
                if (s1.hasNextInt() && s2.hasNextInt()) {
                    c = Integer.compare(s1.nextInt(), s2.nextInt());
                } else {
                    c = s1.next().compareTo(s2.next());
                }
                if (c != 0) {
                    return c;
                }
            }

            if (s1.hasNext()) {
                return 1;
            } else if (s2.hasNext()) {
                return -1;
            }
        }
        return 0;
    }
}
