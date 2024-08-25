package io.github.weimin96.manager.client.utils;

import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * @author panwm
 * @since 2024/8/10 15:42
 */
public class Util {

    public static String getUrl(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        String portString = (port == -1) ? "" : ":" + port;

        return scheme + "://" + host + portString;
    }

    public static String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
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


}
