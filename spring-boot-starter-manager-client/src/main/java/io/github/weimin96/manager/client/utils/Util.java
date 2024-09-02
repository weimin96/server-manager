package io.github.weimin96.manager.client.utils;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest() {
        try {
            return getRequestAttributes().getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, String> getParametersAsMap(HttpServletRequest request) {
        Map<String, String> parameterMap = new HashMap<>();
        Map<String, String[]> parameterMapArray = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : parameterMapArray.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                parameterMap.put(key, values[0]); // Assuming you want the first value if there are multiple values
            }
        }

        return parameterMap;
    }


}
