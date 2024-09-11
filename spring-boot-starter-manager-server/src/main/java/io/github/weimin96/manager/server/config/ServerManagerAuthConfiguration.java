package io.github.weimin96.manager.server.config;

import io.github.weimin96.manager.server.ui.config.ServerManagerUIProperties;
import io.github.weimin96.manager.server.utils.Util;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * @author panwm
 * @since 2024/8/18 0:38
 */
@Configuration
@ConditionalOnProperty(value = "spring.boot.manager.server.authority.enabled", havingValue = "true")
public class ServerManagerAuthConfiguration implements WebFluxConfigurer {

    private final ServerManagerProperties serverProperties;

    private final String publicUrl;

    public ServerManagerAuthConfiguration(ServerManagerProperties serverProperties, ServerManagerUIProperties serverUi) {
        this.serverProperties = serverProperties;
        this.publicUrl = (serverUi.getPublicUrl() != null) ? serverUi.getPublicUrl()
                : this.serverProperties.getContextPath();
    }

    // 白名单路径列表
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/login",
            "/api/login",
            "/assets/"
    );

    @Bean
    public WebFilter serverManagerAuthFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            if (!path.startsWith(this.publicUrl)) {
                return chain.filter(exchange);
            }
            // 检查是否为白名单路径
            if (isWhitelisted(path)) {
                return chain.filter(exchange);
            }
            Mono<WebSession> sessionMono = exchange.getSession();

            return sessionMono.flatMap(session -> {
                Boolean authenticated = checkCredentials(exchange.getRequest(), session);
                if (Boolean.TRUE.equals(authenticated)) {
                    // 已经登录，继续处理请求
                    return chain.filter(exchange);
                } else {
                    if (path.startsWith(publicUrl + "/api")) {
                        // 未授权，返回 JSON 格式的 401 错误
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        String jsonResponse = "{\"error\": \"Unauthorized\", \"message\": \"You need to log in.\"}";
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes());
                        return exchange.getResponse().writeWith(Mono.just(buffer));
                    }
                    // 未登录，重定向到登录页面
                    exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                    exchange.getResponse().getHeaders().setLocation(URI.create(publicUrl + "/login"));
                    return exchange.getResponse().setComplete();
                }
            });
        };
    }

    // 检查路径是否在白名单中
    private boolean isWhitelisted(String path) {
        // 遍历白名单列表，支持通配符匹配（这里使用简单的startsWith作为示例）
        return WHITE_LIST.stream().anyMatch(item -> path.startsWith(publicUrl + item));
    }

    public boolean checkCredentials(ServerHttpRequest request, WebSession session) {
        Boolean authenticated = session.getAttribute("authenticated");
        if (authenticated != null && authenticated) {
            return true;
        }
        String authorization = Util.getParam(request.getURI(), "Authorization");
        if (!StringUtils.hasText(authorization)) {
            authorization = request.getHeaders().getFirst("Authorization");
        }
        if (authorization == null || !authorization.startsWith("Basic")) {
            return false;
        }
        // 提取 Base64 编码的部分
        String base64Credentials = authorization.substring("Basic".length()).trim();

        // Base64 解码
        byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
        String decodedString = new String(decodedBytes);

        // 分割用户名和密码
        String[] credentials = decodedString.split(":", 2);
        if (credentials.length == 2) {
            String username = credentials[0];
            String password = credentials[1];
            return username.equals(this.serverProperties.getAuthority().getDefaultUserName()) &&
                    password.equals(this.serverProperties.getAuthority().getDefaultPassword());
        } else {
            return false;
        }
    }
}
