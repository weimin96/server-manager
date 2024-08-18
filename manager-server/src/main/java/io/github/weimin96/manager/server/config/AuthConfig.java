package io.github.weimin96.manager.server.config;

import io.github.weimin96.manager.server.ui.config.ServerManagerUIProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * @author panwm
 * @since 2024/8/18 0:38
 */
@Configuration
public class AuthConfig implements WebFluxConfigurer {

    private final ServerManagerProperties serverProperties;

    private final ServerManagerUIProperties serverUi;

    private final String publicUrl;

    public AuthConfig(ServerManagerProperties serverProperties, ServerManagerUIProperties serverUi) {
        this.serverProperties = serverProperties;
        this.serverUi = serverUi;
        this.publicUrl = (this.serverUi.getPublicUrl() != null) ? this.serverUi.getPublicUrl()
                : this.serverProperties.getContextPath();
    }

    // 白名单路径列表
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/login",
            "/assets/"
    );

    @Bean
    public WebFilter authFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();

            // 检查是否为白名单路径
            if (isWhitelisted(path)) {
                return chain.filter(exchange);  // 如果在白名单中，直接放行
            }
            Mono<WebSession> sessionMono = exchange.getSession();

            return sessionMono.flatMap(session -> {
                Boolean authenticated = session.getAttribute("authenticated");
                if (Boolean.TRUE.equals(authenticated)) {
                    // 已经登录，继续处理请求
                    return chain.filter(exchange);
                } else {
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
}
