package io.github.weimin96.manager.server.config;

import io.github.weimin96.manager.server.eventstore.InstanceEventStore;
import io.github.weimin96.manager.server.services.ApplicationRegistry;
import io.github.weimin96.manager.server.services.InstanceRegistry;
import io.github.weimin96.manager.server.web.ApplicationsController;
import io.github.weimin96.manager.server.web.InstancesController;
import io.github.weimin96.manager.server.web.client.InstanceWebClient;
import io.github.weimin96.manager.server.web.reactive.ServerControllerHandlerMapping;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

/**
 * @author panwm
 * @since 2024/8/2 23:12
 */
@Configuration(proxyBeanMethods = false)
public class ServerManagerWebConfiguration {

    private final ServerManagerProperties properties;

    public ServerManagerWebConfiguration(ServerManagerProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public InstancesController instancesController(InstanceRegistry instanceRegistry, InstanceEventStore eventStore) {
        return new InstancesController(instanceRegistry, eventStore);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationsController applicationsController(ApplicationRegistry applicationRegistry) {
        return new ApplicationsController(applicationRegistry);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class ReactiveRestApiConfiguration {

        private final ServerManagerProperties properties;

        public ReactiveRestApiConfiguration(ServerManagerProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean
        public io.github.weimin96.manager.server.web.reactive.InstancesProxyController instancesProxyController(
                InstanceRegistry instanceRegistry, InstanceWebClient.Builder instanceWebClientBuilder) {
            return new io.github.weimin96.manager.server.web.reactive.InstancesProxyController(
                    this.properties.getContextPath(),
                    this.properties.getInstanceProxy().getIgnoredHeaders(), instanceRegistry,
                    instanceWebClientBuilder.build());
        }

        @Bean
        public RequestMappingHandlerMapping serverHandlerMapping(
                RequestedContentTypeResolver webFluxContentTypeResolver) {
            org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping mapping = new ServerControllerHandlerMapping(
                    this.properties.getContextPath());
            mapping.setOrder(0);
            mapping.setContentTypeResolver(webFluxContentTypeResolver);
            return mapping;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @AutoConfigureAfter(WebMvcAutoConfiguration.class)
    public static class ServletRestApiConfiguration {

        private final ServerManagerProperties properties;

        public ServletRestApiConfiguration(ServerManagerProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean
        public io.github.weimin96.manager.server.web.servlet.InstancesProxyController instancesProxyController(
                InstanceRegistry instanceRegistry, InstanceWebClient.Builder instanceWebClientBuilder) {
            return new io.github.weimin96.manager.server.web.servlet.InstancesProxyController(
                    this.properties.getContextPath(),
                    this.properties.getInstanceProxy().getIgnoredHeaders(), instanceRegistry,
                    instanceWebClientBuilder.build());
        }

        @Bean
        public org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping serverHandlerMapping(ContentNegotiationManager contentNegotiationManager) {
            org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mapping = new io.github.weimin96.manager.server.web.servlet.ServerControllerHandlerMapping(properties.getContextPath());
            mapping.setOrder(0);
            // 内容协商管理器 根据request 的accept header来选择返回的数据类型
            mapping.setContentNegotiationManager(contentNegotiationManager);
            return mapping;
        }
    }
}
