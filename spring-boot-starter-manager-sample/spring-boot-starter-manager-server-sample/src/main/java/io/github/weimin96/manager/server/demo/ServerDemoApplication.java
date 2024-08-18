package io.github.weimin96.manager.server.demo;

import io.github.weimin96.manager.server.config.EnableManagerServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author panwm
 * @since 2024/8/3 23:28
 */
@EnableManagerServer
@SpringBootApplication
public class ServerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerDemoApplication.class, args);
    }
}
