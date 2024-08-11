package io.github.weimin96.manager.server.web.client;

/**
 * @author panwm
 * @since 2024/8/5 0:19
 */
@FunctionalInterface
public interface InstanceWebClientCustomizer {

    void customize(InstanceWebClient.Builder instanceWebClientBuilder);
}
