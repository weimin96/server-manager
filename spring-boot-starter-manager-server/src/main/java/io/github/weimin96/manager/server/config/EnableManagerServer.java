package io.github.weimin96.manager.server.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用注解标识
 * @author panwm
 * @since 2024/8/2 23:01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ServerManagerMarkerConfiguration.class)
public @interface EnableManagerServer {
}
