
package io.github.weimin96.manager.server.ui.config;

import io.github.weimin96.manager.server.config.ServerManagerCondition;
import io.github.weimin96.manager.server.config.ServerManagerMarkerConfiguration;
import io.github.weimin96.manager.server.config.ServerManagerProperties;
import io.github.weimin96.manager.server.config.ServerManagerWebClientConfiguration;
import io.github.weimin96.manager.server.ui.web.HomepageForwardingFilterConfig;
import io.github.weimin96.manager.server.ui.web.UIController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * @author pwm
 */
@Slf4j
@AutoConfiguration(after = ServerManagerWebClientConfiguration.class)
@Conditional(ServerManagerCondition.class)
@ConditionalOnBean(ServerManagerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties(ServerManagerUIProperties.class)
public class ServerManagerUiAutoConfiguration {

    private static final List<String> DEFAULT_UI_ROUTES = asList("/applications/**", "/instances/**",
            "/journal/**", "/wallboard/**", "/external/**");

    private static final List<String> DEFAULT_UI_ROUTE_EXCLUDES = asList(
            "/instances/*/actuator/heapdump", "/instances/*/actuator/logfile");

    private final ServerManagerUIProperties serverUi;

    private final ServerManagerProperties serverProperties;

    private final ApplicationContext applicationContext;

    public ServerManagerUiAutoConfiguration(ServerManagerUIProperties serverUi, ServerManagerProperties serverProperties,
                                            ApplicationContext applicationContext) {
        this.serverUi = serverUi;
        this.serverProperties = serverProperties;
        this.applicationContext = applicationContext;
    }

    @Bean
    public CssColorUtils cssColorUtils() {
        return new CssColorUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public UIController homeUiController() {
        UIController.Settings uiSettings = UIController.Settings.builder().brand(this.serverUi.getBrand()).title(this.serverUi.getTitle())
                .icon(this.serverUi.getIcon())
                .favicon(this.serverUi.getFavicon())
                .faviconDanger(this.serverUi.getFaviconDanger())
                .routes(DEFAULT_UI_ROUTES)
                .rememberMeEnabled(this.serverUi.isRememberMeEnabled())
                .externalViews(this.serverUi.getExternalViews())
                .pollTimer(this.serverUi.getPollTimer())
                .viewSettings(this.serverUi.getViewSettings())
                .theme(this.serverUi.getTheme())
                .build();

        String publicUrl = (this.serverUi.getPublicUrl() != null) ? this.serverUi.getPublicUrl()
                : this.serverProperties.getContextPath();
        return new UIController(publicUrl, uiSettings, serverProperties);
    }

    @Bean
    public SpringResourceTemplateResolver serverTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(this.applicationContext);
        resolver.setPrefix(this.serverUi.getTemplateLocation());
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(this.serverUi.isCacheTemplates());
        resolver.setOrder(10);
        resolver.setCheckExistence(true);
        return resolver;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class ReactiveUiConfiguration {

        @Configuration(proxyBeanMethods = false)
        public static class serverUiWebfluxConfig implements WebFluxConfigurer {

            private final ServerManagerUIProperties serverUi;

            private final ServerManagerProperties serverProperties;

            private final WebFluxProperties webFluxProperties;

            public serverUiWebfluxConfig(ServerManagerUIProperties serverUi, ServerManagerProperties serverProperties,
                                        WebFluxProperties webFluxProperties) {
                this.serverUi = serverUi;
                this.serverProperties = serverProperties;
                this.webFluxProperties = webFluxProperties;
            }

            @Bean
            public HomepageForwardingFilterConfig homepageForwardingFilterConfig() {
                String webFluxBasePath = webFluxProperties.getBasePath();
                boolean webfluxBasePathSet = webFluxBasePath != null;
                String homepage = webfluxBasePathSet ? webFluxBasePath + "/" : this.serverProperties.path("/");

                List<String> routesIncludes = DEFAULT_UI_ROUTES.stream()
                        .map((path) -> webfluxBasePathSet ? webFluxBasePath + path : this.serverProperties.path(path))
                        .collect(Collectors.toList());
                routesIncludes.add("");

                List<String> routesExcludes = Stream
                        .concat(DEFAULT_UI_ROUTE_EXCLUDES.stream(), this.serverUi.getAdditionalRouteExcludes().stream())
                        .map((path) -> webfluxBasePathSet ? webFluxBasePath + path : this.serverProperties.path(path))
                        .collect(Collectors.toList());

                return new HomepageForwardingFilterConfig(homepage, routesExcludes, routesIncludes);
            }

            @Override
            public void addResourceHandlers(org.springframework.web.reactive.config.ResourceHandlerRegistry registry) {
                registry.addResourceHandler(this.serverProperties.path("/**"))
                        .addResourceLocations(this.serverUi.getResourceLocations())
                        .setCacheControl(this.serverUi.getCache().toCacheControl());
            }

            @Bean
            @ConditionalOnMissingBean
            public io.github.weimin96.manager.server.ui.web.reactive.HomepageForwardingFilter homepageForwardFilter(
                    HomepageForwardingFilterConfig homepageForwardingFilterConfig) throws IOException {
                return new io.github.weimin96.manager.server.ui.web.reactive.HomepageForwardingFilter(
                        homepageForwardingFilterConfig);
            }

        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class ServletUiConfiguration {

        @Configuration(proxyBeanMethods = false)
        public static class serverUiWebMvcConfig implements WebMvcConfigurer {

            private final ServerManagerUIProperties serverUi;

            private final ServerManagerProperties serverProperties;

            public serverUiWebMvcConfig(ServerManagerUIProperties serverUi, ServerManagerProperties serverProperties) {
                this.serverUi = serverUi;
                this.serverProperties = serverProperties;
            }

            @Bean
            public HomepageForwardingFilterConfig homepageForwardingFilterConfig() {
                String homepage = this.serverProperties.path("/");
                List<String> routesIncludes = DEFAULT_UI_ROUTES.stream()
                        .map(this.serverProperties::path).collect(Collectors.toList());
                List<String> routesExcludes = Stream
                        .concat(DEFAULT_UI_ROUTE_EXCLUDES.stream(), this.serverUi.getAdditionalRouteExcludes().stream())
                        .map(this.serverProperties::path).collect(Collectors.toList());

                return new HomepageForwardingFilterConfig(homepage, routesExcludes, routesIncludes);
            }

            @Override
            public void addResourceHandlers(
                    org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
                registry.addResourceHandler(this.serverProperties.path("/**"))
                        .addResourceLocations(this.serverUi.getResourceLocations())
                        .setCacheControl(this.serverUi.getCache().toCacheControl());
            }

            @Bean
            @ConditionalOnMissingBean
            public io.github.weimin96.manager.server.ui.web.servlet.HomepageForwardingFilter homepageForwardFilter(
                    HomepageForwardingFilterConfig homepageForwardingFilterConfig) throws IOException {
                return new io.github.weimin96.manager.server.ui.web.servlet.HomepageForwardingFilter(
                        homepageForwardingFilterConfig);
            }

        }

    }

}
