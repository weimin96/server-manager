package io.github.weimin96.manager.server.config;

import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import io.github.weimin96.manager.server.web.client.*;
import io.github.weimin96.manager.server.web.client.cookies.CookieStoreCleanupTrigger;
import io.github.weimin96.manager.server.web.client.cookies.JdkPerInstanceCookieStore;
import io.github.weimin96.manager.server.web.client.cookies.PerInstanceCookieStore;
import io.github.weimin96.manager.server.web.client.reactive.CompositeReactiveHttpHeadersProvider;
import io.github.weimin96.manager.server.web.client.reactive.ReactiveHttpHeadersProvider;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.CookiePolicy;
import java.util.List;

/**
 * @author panwm
 * @since 2024/8/5 0:17
 */
@Configuration(proxyBeanMethods = false)
@Lazy(false)
public class ServerManagerWebClientConfiguration {

    private final InstanceWebClient.Builder instanceWebClientBuilder;

    public ServerManagerWebClientConfiguration(ObjectProvider<InstanceWebClientCustomizer> customizers,
                                               WebClient.Builder webClient) {
        this.instanceWebClientBuilder = InstanceWebClient.builder(webClient);
        customizers.orderedStream().forEach((customizer) -> customizer.customize(this.instanceWebClientBuilder));
    }

    @Bean
    @ConditionalOnMissingBean
    @Scope("prototype")
    public InstanceWebClient.Builder instanceWebClientBuilder() {
        return this.instanceWebClientBuilder.clone();
    }

    @Configuration(proxyBeanMethods = false)
    protected static class InstanceExchangeFiltersConfiguration {

        @Bean
        @ConditionalOnBean(InstanceExchangeFilterFunction.class)
        @ConditionalOnMissingBean(name = "filterInstanceWebClientCustomizer")
        public InstanceWebClientCustomizer filterInstanceWebClientCustomizer(
                List<InstanceExchangeFilterFunction> filters) {
            return (builder) -> builder.filters((f) -> f.addAll(filters));
        }

        @Configuration(proxyBeanMethods = false)
        protected static class DefaultInstanceExchangeFiltersConfiguration {

            @Bean
            @Order(0)
            @ConditionalOnBean(HttpHeadersProvider.class)
            @ConditionalOnMissingBean(name = "addHeadersInstanceExchangeFilter")
            public InstanceExchangeFilterFunction addHeadersInstanceExchangeFilter(
                    List<HttpHeadersProvider> headersProviders) {
                return InstanceExchangeFilterFunctions.addHeaders(new CompositeHttpHeadersProvider(headersProviders));
            }

            @Bean
            @Order(0)
            @ConditionalOnBean(ReactiveHttpHeadersProvider.class)
            @ConditionalOnMissingBean(name = "addReactiveHeadersInstanceExchangeFilter")
            public InstanceExchangeFilterFunction addReactiveHeadersInstanceExchangeFilter(
                    List<ReactiveHttpHeadersProvider> reactiveHeadersProviders) {
                return InstanceExchangeFilterFunctions
                        .addHeadersReactive(new CompositeReactiveHttpHeadersProvider(reactiveHeadersProviders));
            }

            @Bean
            @Order(10)
            @ConditionalOnMissingBean(name = "rewriteEndpointUrlInstanceExchangeFilter")
            public InstanceExchangeFilterFunction rewriteEndpointUrlInstanceExchangeFilter() {
                return InstanceExchangeFilterFunctions.rewriteEndpointUrl();
            }

            @Bean
            @Order(20)
            @ConditionalOnMissingBean(name = "setDefaultAcceptHeaderInstanceExchangeFilter")
            public InstanceExchangeFilterFunction setDefaultAcceptHeaderInstanceExchangeFilter() {
                return InstanceExchangeFilterFunctions.setDefaultAcceptHeader();
            }

            @Bean
            @Order(30)
            @ConditionalOnBean(LegacyEndpointConverter.class)
            @ConditionalOnMissingBean(name = "legacyEndpointConverterInstanceExchangeFilter")
            public InstanceExchangeFilterFunction legacyEndpointConverterInstanceExchangeFilter(
                    List<LegacyEndpointConverter> converters) {
                return InstanceExchangeFilterFunctions.convertLegacyEndpoints(converters);
            }

            @Bean
            @Order(40)
            @ConditionalOnMissingBean(name = "logfileAcceptWorkaround")
            public InstanceExchangeFilterFunction logfileAcceptWorkaround() {
                return InstanceExchangeFilterFunctions.logfileAcceptWorkaround();
            }

            @Bean
            @Order(50)
            @ConditionalOnMissingBean(name = "cookieHandlingInstanceExchangeFilter")
            public InstanceExchangeFilterFunction cookieHandlingInstanceExchangeFilter(
                    final PerInstanceCookieStore store) {
                return InstanceExchangeFilterFunctions.handleCookies(store);
            }

            @Bean
            @Order(100)
            @ConditionalOnMissingBean(name = "retryInstanceExchangeFilter")
            public InstanceExchangeFilterFunction retryInstanceExchangeFilter(
                    ServerManagerProperties properties) {
                ServerManagerProperties.MonitorProperties monitor = properties.getMonitor();
                return InstanceExchangeFilterFunctions.retry(monitor.getDefaultRetries(), monitor.getRetries());
            }

            @Bean
            @Order(200)
            @ConditionalOnMissingBean(name = "timeoutInstanceExchangeFilter")
            public InstanceExchangeFilterFunction timeoutInstanceExchangeFilter(
                    ServerManagerProperties properties) {
                ServerManagerProperties.MonitorProperties monitor = properties.getMonitor();
                return InstanceExchangeFilterFunctions.timeout(monitor.getDefaultTimeout(), monitor.getTimeout());
            }

        }

    }

    @Configuration(proxyBeanMethods = false)
    protected static class HttpHeadersProviderConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public BasicAuthHttpHeaderProvider basicAuthHttpHeadersProvider(ServerManagerProperties properties) {
            ServerManagerProperties.InstanceAuthProperties instanceAuth = properties.getInstanceAuth();

            if (instanceAuth.isEnabled()) {
                return new BasicAuthHttpHeaderProvider(instanceAuth.getDefaultUserName(),
                        instanceAuth.getDefaultPassword(), instanceAuth.getServiceMap());
            }
            else {
                return new BasicAuthHttpHeaderProvider();
            }
        }

    }

    @Configuration(proxyBeanMethods = false)
    protected static class LegaycEndpointConvertersConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "healthLegacyEndpointConverter")
        public LegacyEndpointConverter healthLegacyEndpointConverter() {
            return LegacyEndpointConverters.health();
        }

        @Bean
        @ConditionalOnMissingBean(name = "infoLegacyEndpointConverter")
        public LegacyEndpointConverter infoLegacyEndpointConverter() {
            return LegacyEndpointConverters.info();
        }

        @Bean
        @ConditionalOnMissingBean(name = "envLegacyEndpointConverter")
        public LegacyEndpointConverter envLegacyEndpointConverter() {
            return LegacyEndpointConverters.env();
        }

        @Bean
        @ConditionalOnMissingBean(name = "httptraceLegacyEndpointConverter")
        public LegacyEndpointConverter httptraceLegacyEndpointConverter() {
            return LegacyEndpointConverters.httptrace();
        }

        @Bean
        @ConditionalOnMissingBean(name = "threaddumpLegacyEndpointConverter")
        public LegacyEndpointConverter threaddumpLegacyEndpointConverter() {
            return LegacyEndpointConverters.threaddump();
        }

        @Bean
        @ConditionalOnMissingBean(name = "liquibaseLegacyEndpointConverter")
        public LegacyEndpointConverter liquibaseLegacyEndpointConverter() {
            return LegacyEndpointConverters.liquibase();
        }

        @Bean
        @ConditionalOnMissingBean(name = "flywayLegacyEndpointConverter")
        public LegacyEndpointConverter flywayLegacyEndpointConverter() {
            return LegacyEndpointConverters.flyway();
        }

        @Bean
        @ConditionalOnMissingBean(name = "beansLegacyEndpointConverter")
        public LegacyEndpointConverter beansLegacyEndpointConverter() {
            return LegacyEndpointConverters.beans();
        }

        @Bean
        @ConditionalOnMissingBean(name = "configpropsLegacyEndpointConverter")
        public LegacyEndpointConverter configpropsLegacyEndpointConverter() {
            return LegacyEndpointConverters.configprops();
        }

        @Bean
        @ConditionalOnMissingBean(name = "mappingsLegacyEndpointConverter")
        public LegacyEndpointConverter mappingsLegacyEndpointConverter() {
            return LegacyEndpointConverters.mappings();
        }

        @Bean
        @ConditionalOnMissingBean(name = "startupLegacyEndpointConverter")
        public LegacyEndpointConverter startupLegacyEndpointConverter() {
            return LegacyEndpointConverters.startup();
        }

    }

    @Configuration(proxyBeanMethods = false)
    protected static class CookieStoreConfiguration {

        /**
         * Creates a default {@link PerInstanceCookieStore} that should be used.
         * @return the cookie store
         */
        @Bean
        @ConditionalOnMissingBean
        public PerInstanceCookieStore cookieStore() {
            return new JdkPerInstanceCookieStore(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        }

        @Bean(initMethod = "start", destroyMethod = "stop")
        @ConditionalOnMissingBean
        public CookieStoreCleanupTrigger cookieStoreCleanupTrigger(final Publisher<InstanceEvent> publisher,
                                                                   final PerInstanceCookieStore cookieStore) {
            return new CookieStoreCleanupTrigger(publisher, cookieStore);
        }
    }
}
