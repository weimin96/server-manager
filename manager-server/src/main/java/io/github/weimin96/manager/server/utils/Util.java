package io.github.weimin96.manager.server.utils;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * @author panwm
 * @since 2024/8/10 15:42
 */
public class Util {

    public static String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        String normalizedPath = path;
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        if (normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        return normalizedPath;
    }

    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
