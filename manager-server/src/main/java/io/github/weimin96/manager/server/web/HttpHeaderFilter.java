package io.github.weimin96.manager.server.web;

import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * 请求头过滤器
 * @author panwm
 * @since 2024/8/4 23:36
 */
public class HttpHeaderFilter {

    /**
     * 默认忽略的请求头
     */
    private static final String[] HOP_BY_HOP_HEADERS = new String[] { "Host", "Connection", "Keep-Alive",
            "Proxy-Authenticate", "Proxy-Authorization", "TE", "Trailer", "Transfer-Encoding", "Upgrade",
            "X-Application-Context" };

    private final Set<String> ignoredHeaders;

    public HttpHeaderFilter(Set<String> ignoredHeaders) {
        this.ignoredHeaders = Stream.concat(ignoredHeaders.stream(), Arrays.stream(HOP_BY_HOP_HEADERS))
                .map(String::toLowerCase).collect(Collectors.toSet());
    }

    public HttpHeaders filterHeaders(HttpHeaders headers) {
        HttpHeaders filtered = new HttpHeaders();
        filtered.putAll(headers.entrySet().stream().filter((e) -> this.includeHeader(e.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return filtered;
    }

    private boolean includeHeader(String header) {
        return !this.ignoredHeaders.contains(header.toLowerCase());
    }

}
