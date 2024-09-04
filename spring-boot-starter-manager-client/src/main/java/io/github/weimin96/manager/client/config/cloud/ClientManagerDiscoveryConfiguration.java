package io.github.weimin96.manager.client.config.cloud;

import io.github.weimin96.manager.client.config.ClientProperties;
import io.github.weimin96.manager.client.config.InstanceProperties;
import io.github.weimin96.manager.client.utils.Util;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;

/**
 * @author panwm
 * @since 2024/8/22 17:38
 */
public class ClientManagerDiscoveryConfiguration {

    private final DiscoveryClient discoveryClient;

    private final ClientProperties clientProperties;

    private final InstanceProperties instanceProperties;

    public ClientManagerDiscoveryConfiguration(DiscoveryClient discoveryClient, ClientProperties clientProperties, InstanceProperties instanceProperties) {
        this.discoveryClient = discoveryClient;
        this.clientProperties = clientProperties;
        this.instanceProperties = instanceProperties;
    }

    public void setServerInfo() {
        if (clientProperties.getUrl().length == 0 && StringUtils.hasText(clientProperties.getServerApplicationName())) {
            // 设置url
            List<ServiceInstance> instances = discoveryClient.getInstances(clientProperties.getServerApplicationName());
            if (instances != null && !instances.isEmpty()) {
                clientProperties.setUrl(instances.stream()
                        .map(e -> Util.getUrl(e.getUri()) + Util.normalizePath(clientProperties.getServerContextPath()))
                        .toArray(String[]::new));
            }
        }
    }
}
