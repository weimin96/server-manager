package io.github.weimin96.manager.client.config.cloud;

import io.github.weimin96.manager.client.config.ClientManagerAutoConfiguration;
import io.github.weimin96.manager.client.config.ClientProperties;
import io.github.weimin96.manager.client.utils.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author panwm
 * @since 2024/8/22 17:38
 */
@AutoConfiguration(after = ClientManagerAutoConfiguration.class)
@ConditionalOnBean(DiscoveryClient.class)
@Slf4j
public class ClientManagerDiscoveryConfiguration {

    private final DiscoveryClient discoveryClient;

    private final ClientProperties clientProperties;

    private volatile ScheduledFuture<?> scheduledTask;

    private ThreadPoolTaskScheduler taskScheduler;

    private boolean firstAttempt = true;

    private Duration listenerNacosPeriod = Duration.ofSeconds(10);

    public ClientManagerDiscoveryConfiguration(DiscoveryClient discoveryClient, ClientProperties clientProperties) {
        this.discoveryClient = discoveryClient;
        this.clientProperties = clientProperties;
        this.taskScheduler = registrationNacosTaskScheduler();
    }

    @EventListener
    @Order()
    public void onReady(ApplicationReadyEvent event) {
        // 定时从nacos获取服务端地址
        this.scheduledTask = this.taskScheduler.scheduleAtFixedRate(this::setServerInfo, listenerNacosPeriod);
    }

    @EventListener
    @Order()
    public void onClose(ContextClosedEvent event) {
        // 定时从nacos获取服务端地址
        this.scheduledTask.cancel(true);
        this.taskScheduler.destroy();
    }

    private static ThreadPoolTaskScheduler registrationNacosTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setRemoveOnCancelPolicy(true);
        taskScheduler.setThreadNamePrefix("registrationNacosTask");
        taskScheduler.initialize();
        return taskScheduler;
    }

    public void setServerInfo() {
        if (StringUtils.hasText(clientProperties.getServerApplicationName())) {
            // 设置url
            List<ServiceInstance> instances = discoveryClient.getInstances(clientProperties.getServerApplicationName());
            if (instances != null && !instances.isEmpty()) {
                clientProperties.setUrl(instances.stream()
                        .map(e -> Util.getUrl(e.getUri()) + Util.normalizePath(clientProperties.getServerContextPath()))
                        .toArray(String[]::new));
            } else {
                if (firstAttempt) {
                    log.error("无法从nacos获取server服务地址");
                    firstAttempt = false;
                }
            }
        }
    }
}
