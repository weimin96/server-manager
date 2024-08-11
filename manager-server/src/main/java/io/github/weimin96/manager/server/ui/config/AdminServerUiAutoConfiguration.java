
package io.github.weimin96.manager.server.ui.config;

import io.github.weimin96.manager.server.config.ServerManagerCondition;
import io.github.weimin96.manager.server.config.ServerManagerMarkerConfiguration;
import io.github.weimin96.manager.server.config.ServerManagerProperties;
import io.github.weimin96.manager.server.config.ServerManagerWebClientConfiguration;
import io.github.weimin96.manager.server.ui.web.HomepageForwardingFilterConfig;
import io.github.weimin96.manager.server.ui.web.UiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@AutoConfiguration(after = ServerManagerWebClientConfiguration.class)
@Conditional(ServerManagerCondition.class)
@ConditionalOnBean(ServerManagerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties(AdminServerUiProperties.class)
public class AdminServerUiAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AdminServerUiAutoConfiguration.class);

    private static final List<String> DEFAULT_UI_ROUTES = asList("/about/**", "/applications/**", "/instances/**",
            "/journal/**", "/wallboard/**", "/external/**");

    private static final List<String> DEFAULT_UI_ROUTE_EXCLUDES = asList("/extensions/**",
            "/instances/*/actuator/heapdump", "/instances/*/actuator/logfile");

    private final AdminServerUiProperties adminUi;

    private final ServerManagerProperties adminServer;

    private final ApplicationContext applicationContext;

    public AdminServerUiAutoConfiguration(AdminServerUiProperties adminUi, ServerManagerProperties serverProperties,
                                          ApplicationContext applicationContext) {
        this.adminUi = adminUi;
        this.adminServer = serverProperties;
        this.applicationContext = applicationContext;
    }

    @Bean
    public CssColorUtils cssColorUtils() {
        return new CssColorUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public UiController homeUiController() throws IOException {

        UiController.Settings uiSettings = UiController.Settings.builder().brand(this.adminUi.getBrand()).title(this.adminUi.getTitle())
                .loginIcon(this.adminUi.getLoginIcon())
                .favicon(this.adminUi.getFavicon())
                .faviconDanger(this.adminUi.getFaviconDanger())
                .enableToasts(this.adminUi.getEnableToasts())
                .rememberMeEnabled(this.adminUi.isRememberMeEnabled())
                .availableLanguages(this.adminUi.getAvailableLanguages())
                .externalViews(this.adminUi.getExternalViews())
                .pollTimer(this.adminUi.getPollTimer())
                .viewSettings(this.adminUi.getViewSettings())
                .theme(this.adminUi.getTheme())
                .build();

        String publicUrl = (this.adminUi.getPublicUrl() != null) ? this.adminUi.getPublicUrl()
                : this.adminServer.getContextPath();
        return new UiController(publicUrl, uiSettings);
    }

    @Bean
    public SpringResourceTemplateResolver adminTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(this.applicationContext);
        resolver.setPrefix(this.adminUi.getTemplateLocation());
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(this.adminUi.isCacheTemplates());
        resolver.setOrder(10);
        resolver.setCheckExistence(true);
        return resolver;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class ReactiveUiConfiguration {

        @Configuration(proxyBeanMethods = false)
        public static class AdminUiWebfluxConfig implements WebFluxConfigurer {

            private final AdminServerUiProperties adminUi;

            private final ServerManagerProperties adminServer;

            private final WebFluxProperties webFluxProperties;

            public AdminUiWebfluxConfig(AdminServerUiProperties adminUi, ServerManagerProperties adminServer,
                                        WebFluxProperties webFluxProperties) {
                this.adminUi = adminUi;
                this.adminServer = adminServer;
                this.webFluxProperties = webFluxProperties;
            }

            @Bean
            public HomepageForwardingFilterConfig homepageForwardingFilterConfig() throws IOException {
                String webFluxBasePath = webFluxProperties.getBasePath();
                boolean webfluxBasePathSet = webFluxBasePath != null;
                String homepage = webfluxBasePathSet ? webFluxBasePath + "/" : this.adminServer.path("/");

                List<String> routesExcludes = Stream
                        .concat(DEFAULT_UI_ROUTE_EXCLUDES.stream(), this.adminUi.getAdditionalRouteExcludes().stream())
                        .map((path) -> webfluxBasePathSet ? webFluxBasePath + path : this.adminServer.path(path))
                        .collect(Collectors.toList());

                return new HomepageForwardingFilterConfig(homepage, routesExcludes);
            }

            @Override
            public void addResourceHandlers(org.springframework.web.reactive.config.ResourceHandlerRegistry registry) {
                registry.addResourceHandler(this.adminServer.path("/**"))
                        .addResourceLocations(this.adminUi.getResourceLocations())
                        .setCacheControl(this.adminUi.getCache().toCacheControl());
                registry.addResourceHandler(this.adminServer.path("/extensions/**"))
                        .addResourceLocations(this.adminUi.getExtensionResourceLocations())
                        .setCacheControl(this.adminUi.getCache().toCacheControl());
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
        public static class AdminUiWebMvcConfig implements WebMvcConfigurer {

            private final AdminServerUiProperties adminUi;

            private final ServerManagerProperties adminServer;

            public AdminUiWebMvcConfig(AdminServerUiProperties adminUi, ServerManagerProperties adminServer) {
                this.adminUi = adminUi;
                this.adminServer = adminServer;
            }

            @Bean
            public HomepageForwardingFilterConfig homepageForwardingFilterConfig() throws IOException {
                String homepage = this.adminServer.path("/");

                List<String> routesExcludes = Stream
                        .concat(DEFAULT_UI_ROUTE_EXCLUDES.stream(), this.adminUi.getAdditionalRouteExcludes().stream())
                        .map(this.adminServer::path).collect(Collectors.toList());

                return new HomepageForwardingFilterConfig(homepage, routesExcludes);
            }

            @Override
            public void addResourceHandlers(
                    org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
                registry.addResourceHandler(this.adminServer.path("/**"))
                        .addResourceLocations(this.adminUi.getResourceLocations())
                        .setCacheControl(this.adminUi.getCache().toCacheControl());
                registry.addResourceHandler(this.adminServer.path("/extensions/**"))
                        .addResourceLocations(this.adminUi.getExtensionResourceLocations())
                        .setCacheControl(this.adminUi.getCache().toCacheControl());
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
