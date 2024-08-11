package io.github.weimin96.manager.server.config;

import io.github.weimin96.manager.server.domain.entity.EventInstanceRepository;
import io.github.weimin96.manager.server.domain.entity.InstanceRepository;
import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import io.github.weimin96.manager.server.eventstore.InstanceEventPublisher;
import io.github.weimin96.manager.server.eventstore.InstanceEventStore;
import io.github.weimin96.manager.server.services.*;
import io.github.weimin96.manager.server.services.endpoints.ChainingStrategy;
import io.github.weimin96.manager.server.services.endpoints.ProbeEndpointsStrategy;
import io.github.weimin96.manager.server.services.endpoints.QueryIndexEndpointStrategy;
import io.github.weimin96.manager.server.web.client.InstanceWebClient;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;

/**
 * @author panwm
 * @since 2024/8/2 22:45
 */
@AutoConfiguration(after =WebClientAutoConfiguration.class)
@Conditional(ServerManagerCondition.class)
@ConditionalOnBean(ServerManagerMarkerConfiguration.Marker.class)
@ImportAutoConfiguration({ServerManagerWebClientConfiguration.class, ServerManagerWebConfiguration.class})
@EnableConfigurationProperties(ServerManagerProperties.class)
@Lazy(false)
public class ServerManagerAutoConfiguration {

    private final ServerManagerProperties properties;

    public ServerManagerAutoConfiguration(ServerManagerProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public InstanceRegistry instanceRegistry(InstanceRepository instanceRepository,
                                             InstanceIdGenerator instanceIdGenerator) {
        return new InstanceRegistry(instanceRepository, instanceIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationRegistry applicationRegistry(InstanceRegistry instanceRegistry,
                                                   InstanceEventPublisher instanceEventPublisher) {
        return new ApplicationRegistry(instanceRegistry, instanceEventPublisher);
    }

    /**
     * 实例id生成器
     * @return InstanceIdGenerator
     */
    @Bean
    @ConditionalOnMissingBean
    public InstanceIdGenerator instanceIdGenerator() {
        return new InstanceIdGenerator();
    }


    @Bean
    @ConditionalOnMissingBean
    public StatusUpdater statusUpdater(InstanceRepository instanceRepository,
                                       InstanceWebClient.Builder instanceWebClientBulder) {
        return new StatusUpdater(instanceRepository, instanceWebClientBulder.build(), new ApiMediaTypeHandler());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public StatusUpdateTrigger statusUpdateTrigger(StatusUpdater statusUpdater, Publisher<InstanceEvent> events) {
        StatusUpdateTrigger trigger = new StatusUpdateTrigger(statusUpdater, events);
        trigger.setInterval(this.properties.getMonitor().getStatusInterval());
        trigger.setLifetime(this.properties.getMonitor().getStatusLifetime());
        return trigger;
    }

    @Bean
    @ConditionalOnMissingBean
    public EndpointDetector endpointDetector(InstanceRepository instanceRepository,
                                             InstanceWebClient.Builder instanceWebClientBuilder) {
        InstanceWebClient instanceWebClient = instanceWebClientBuilder.build();
        ChainingStrategy strategy = new ChainingStrategy(
                new QueryIndexEndpointStrategy(instanceWebClient, new ApiMediaTypeHandler()),
                new ProbeEndpointsStrategy(instanceWebClient, this.properties.getProbedEndpoints()));
        return new EndpointDetector(instanceRepository, strategy);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public EndpointDetectionTrigger endpointDetectionTrigger(EndpointDetector endpointDetector,
                                                             Publisher<InstanceEvent> events) {
        return new EndpointDetectionTrigger(endpointDetector, events);
    }

    @Bean
    @ConditionalOnMissingBean
    public InfoUpdater infoUpdater(InstanceRepository instanceRepository,
                                   InstanceWebClient.Builder instanceWebClientBuilder) {
        return new InfoUpdater(instanceRepository, instanceWebClientBuilder.build(), new ApiMediaTypeHandler());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public InfoUpdateTrigger infoUpdateTrigger(InfoUpdater infoUpdater, Publisher<InstanceEvent> events) {
        InfoUpdateTrigger trigger = new InfoUpdateTrigger(infoUpdater, events);
        trigger.setInterval(this.properties.getMonitor().getInfoInterval());
        trigger.setLifetime(this.properties.getMonitor().getInfoLifetime());
        return trigger;
    }

    @Bean
    @ConditionalOnMissingBean(InstanceEventStore.class)
    public InstanceEventStore eventStore() {
        return new InstanceEventStore();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(InstanceRepository.class)
    public EventInstanceRepository instanceRepository(InstanceEventStore eventStore) {
        return new EventInstanceRepository(eventStore);
    }
}
