package io.github.weimin96.manager.server.utils;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public static String getParam(URI uri, String key) {
        if (uri == null || key == null || key.isEmpty()) {
            return null;
        }
        try {
            // 获取查询部分
            String query = uri.getQuery();
            if (query == null) {
                return null;
            }
            Map<String, String> queryParams = parseQueryParams(query);
            return queryParams.entrySet().stream().filter(e -> key.equals(e.getKey())).findFirst().map(Map.Entry::getValue).orElse(null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析查询参数
     * @param query 查询字符串
     * @return 参数映射
     */
    private static Map<String, String> parseQueryParams(String query) throws UnsupportedEncodingException {
        Map<String, String> queryParams = new HashMap<>();
        String[] pairs = query.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                queryParams.put(key, value);
            }
        }
        return queryParams;
    }
}
