package io.github.weimin96.manager.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 标记配置类 与EnableManagerAdminServer注解、AdminServerManagerAutoConfiguration配置类结合使用
 * @author panwm
 * @since 2024/8/2 23:02
 */
// false 直接返回新实例对象，Spring启动速度会更快
@Configuration(proxyBeanMethods = false)
public class ServerManagerMarkerConfiguration {

    @Bean
    public Marker adminServerMarker() {
        return new Marker();
    }

    public static class Marker {

    }
}
