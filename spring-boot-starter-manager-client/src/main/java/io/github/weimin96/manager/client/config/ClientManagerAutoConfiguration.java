
package io.github.weimin96.manager.client.config;

import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import io.github.weimin96.manager.client.config.cloud.ClientManagerDiscoveryConfiguration;
import io.github.weimin96.manager.client.endpoints.druid.DruidStateEndpoint;
import io.github.weimin96.manager.client.endpoints.log.LogContentEndpoint;
import io.github.weimin96.manager.client.endpoints.log.LogDirEndpoint;
import io.github.weimin96.manager.client.registration.*;
import io.github.weimin96.manager.client.registration.metadata.CompositeMetadataContributor;
import io.github.weimin96.manager.client.registration.metadata.MetadataContributor;
import io.github.weimin96.manager.client.registration.metadata.StartupDateMetadataContributor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.List;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

/**
 * @author pwm
 */
@AutoConfiguration(after = {WebEndpointAutoConfiguration.class, RestTemplateAutoConfiguration.class,
        WebClientAutoConfiguration.class})
@ConditionalOnWebApplication
@Conditional(ClientManagerCondition.class)
@EnableConfigurationProperties({ClientProperties.class, InstanceProperties.class, ServerProperties.class,
        ManagementServerProperties.class})
@ImportAutoConfiguration({LogDirEndpoint.class, LogContentEndpoint.class})
public class ClientManagerAutoConfiguration {

    @Bean
    @ConditionalOnBean(DiscoveryClient.class)
    @ConditionalOnMissingBean
    @Order(1)
    public ClientManagerDiscoveryConfiguration clientManagerDiscoveryConfiguration(DiscoveryClient discoveryClient, ClientProperties clientProperties, InstanceProperties instanceProperties) {
        ClientManagerDiscoveryConfiguration clientManagerDiscoveryConfiguration = new ClientManagerDiscoveryConfiguration(discoveryClient, clientProperties, instanceProperties);
        clientManagerDiscoveryConfiguration.setServerInfo();
        return clientManagerDiscoveryConfiguration;
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(2)
    public ApplicationRegistrator registrator(RegistrationClient registrationClient, ClientProperties client,
                                              ApplicationFactory applicationFactory, ClientManagerDiscoveryConfiguration clientManagerDiscoveryConfiguration) {

        return new DefaultApplicationRegistrator(applicationFactory, registrationClient, client ,clientManagerDiscoveryConfiguration,
                client.isRegisterOnce());
    }

    @Bean
    @ConditionalOnBean(DruidStatProperties.class)
    @ConditionalOnMissingBean
    public DruidStateEndpoint druidStateEndpoint() {
        return new DruidStateEndpoint();
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistrationApplicationListener registrationListener(ClientProperties client,
                                                                ApplicationRegistrator registrator, Environment environment) {
        RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator);
        listener.setAutoRegister(client.isAutoRegistration());
        listener.setAutoDeregister(client.isAutoDeregistration(environment));
        listener.setRegisterPeriod(client.getPeriod());
        return listener;
    }

    @Bean
    @ConditionalOnMissingBean
    public StartupDateMetadataContributor startupDateMetadataContributor() {
        return new StartupDateMetadataContributor();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @AutoConfigureAfter(DispatcherServletAutoConfiguration.class)
    public static class ServletConfiguration {

        @Bean
        @Lazy(false)
        @ConditionalOnMissingBean
        public ApplicationFactory applicationFactory(InstanceProperties instance, ManagementServerProperties management,
                                                     ServerProperties server, ServletContext servletContext, PathMappedEndpoints pathMappedEndpoints,
                                                     WebEndpointProperties webEndpoint, ObjectProvider<List<MetadataContributor>> metadataContributors,
                                                     DispatcherServletPath dispatcherServletPath) {
            return new ServletApplicationFactory(instance, management, server, servletContext, pathMappedEndpoints,
                    webEndpoint,
                    new CompositeMetadataContributor(metadataContributors.getIfAvailable(Collections::emptyList)),
                    dispatcherServletPath);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    public static class ReactiveConfiguration {

        @Bean
        @Lazy(false)
        @ConditionalOnMissingBean
        public ApplicationFactory applicationFactory(InstanceProperties instance, ManagementServerProperties management,
                                                     ServerProperties server, PathMappedEndpoints pathMappedEndpoints, WebEndpointProperties webEndpoint,
                                                     ObjectProvider<List<MetadataContributor>> metadataContributors, WebFluxProperties webFluxProperties) {
            return new ReactiveApplicationFactory(instance, management, server, pathMappedEndpoints, webEndpoint,
                    new CompositeMetadataContributor(metadataContributors.getIfAvailable(Collections::emptyList)),
                    webFluxProperties);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(RestTemplateBuilder.class)
    public static class BlockingRegistrationClientConfig {

        @Bean
        @ConditionalOnMissingBean
        public RegistrationClient registrationClient(ClientProperties client) {
            RestTemplateBuilder builder = new RestTemplateBuilder().setConnectTimeout(client.getConnectTimeout())
                    .setReadTimeout(client.getReadTimeout());
            if (client.getUsername() != null && client.getPassword() != null) {
                builder = builder.basicAuthentication(client.getUsername(), client.getPassword());
            }
            return new BlockingRegistrationClient(builder.build());
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(WebClient.Builder.class)
    @ConditionalOnMissingBean(RestTemplateBuilder.class)
    public static class ReactiveRegistrationClientConfig {

        @Bean
        @ConditionalOnMissingBean
        public RegistrationClient registrationClient(ClientProperties client, WebClient.Builder webClient) {
            if (client.getUsername() != null && client.getPassword() != null) {
                webClient = webClient.filter(basicAuthentication(client.getUsername(), client.getPassword()));
            }
            return new ReactiveRegistrationClient(webClient.build(), client.getReadTimeout());
        }

    }

}
