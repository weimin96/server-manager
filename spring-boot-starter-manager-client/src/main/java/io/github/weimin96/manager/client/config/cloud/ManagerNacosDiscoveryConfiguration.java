package io.github.weimin96.manager.client.config.cloud;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Conditional;

import java.util.List;

/**
 * describe:
 *
 * @author panwm
 * @since 2024/8/22 17:38
 */
public class ManagerNacosDiscoveryConfiguration {

    private final DiscoveryClient discoveryClient;

    public ManagerNacosDiscoveryConfiguration(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public String getServerUrl(String serverApplicationName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serverApplicationName);

        if (instances == null || instances.isEmpty()) {
            return "No instances available";
        }

        // 假设你只需要第一个实例的信息
        ServiceInstance instance = instances.get(0);
        String host = instance.getHost();
        int port = instance.getPort();

        return host + ":" + port;
    }
}
