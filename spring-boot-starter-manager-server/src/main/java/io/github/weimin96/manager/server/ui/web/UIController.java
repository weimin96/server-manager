
package io.github.weimin96.manager.server.ui.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.weimin96.manager.server.config.ServerManagerProperties;
import io.github.weimin96.manager.server.ui.config.ServerManagerUIProperties;
import io.github.weimin96.manager.server.utils.Util;
import io.github.weimin96.manager.server.web.ServerController;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * @author pwm
 */
@ServerController
public class UIController {

    private final String publicUrl;

    private final Settings uiSettings;

    private final ServerManagerProperties properties;

    public UIController(String publicUrl, Settings uiSettings, ServerManagerProperties properties) {
        this.publicUrl = publicUrl;
        this.uiSettings = uiSettings;
        this.properties = properties;
    }

    @ModelAttribute(value = "baseUrl", binding = false)
    public String getBaseUrl(UriComponentsBuilder uriBuilder) {
        UriComponents publicComponents = UriComponentsBuilder.fromUriString(this.publicUrl).build();
        if (publicComponents.getScheme() != null) {
            uriBuilder.scheme(publicComponents.getScheme());
        }
        if (publicComponents.getHost() != null) {
            uriBuilder.host(publicComponents.getHost());
        }
        if (publicComponents.getPort() != -1) {
            uriBuilder.port(publicComponents.getPort());
        }
        if (publicComponents.getPath() != null) {
            uriBuilder.path(publicComponents.getPath());
        }
        return uriBuilder.path("/").toUriString();
    }

    @ModelAttribute(value = "uiSettings", binding = false)
    public Settings getUiSettings() {
        return this.uiSettings;
    }

    @GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "index";
    }

    @GetMapping(path = "/settings.js", produces = "application/javascript")
    public String settings() {
        return "settings.js";
    }

    @GetMapping(path = "/variables.css", produces = "text/css")
    public String variablesCss() {
        return "variables.css";
    }

    @GetMapping(path = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String login() {
        return "login";
    }

    @PostMapping(path = "/api/login")
    @ResponseBody
    public Mono<ResponseEntity<String>> login(@RequestBody LoginForm loginForm,WebSession session) {
        if (!properties.getAuthority().isEnabled()) {
            return Mono.just(ResponseEntity.ok("success"));
        }
        String username = loginForm.getUsername();
        String password = loginForm.getPassword();
        return Mono.fromCallable(() -> {
            boolean validUsername = StringUtils.hasText(username) && username.equals(properties.getAuthority().getDefaultUserName());
            boolean validPassword = StringUtils.hasText(password) && password.equals(properties.getAuthority().getDefaultPassword());

            if (validUsername && validPassword) {
                session.getAttributes().put("authenticated", true);
                String token = Base64Utils.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
                return "Basic " + token;
            } else {
                throw new RuntimeException("Invalid credentials");
            }
        }).map(ResponseEntity::ok).onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登录失败")));
    }

    @Data
    @Builder
    public static class Settings {

        private final String title;

        private final String brand;

        private final String icon;

        private final String favicon;

        private final String faviconDanger;

        private final ServerManagerUIProperties.PollTimer pollTimer;

        private final List<String> routes;

        private final List<ExternalView> externalViews;

        private final List<ViewSettings> viewSettings;
    }

    @Data
    @JsonInclude(Include.NON_EMPTY)
    public static class ExternalView {

        /**
         * Label to be shown in the navbar.
         */
        private final String label;

        /**
         * Url for the external view to be linked
         */
        private final String url;

        /**
         * Order in the navbar.
         */
        private final Integer order;

        /**
         * Should the page shown as an iframe or open in a new window.
         */
        private final boolean iframe;

        /**
         * A list of child views.
         */
        private final List<ExternalView> children;

        public ExternalView(String label, String url, Integer order, boolean iframe, List<ExternalView> children) {
            Assert.hasText(label, "'label' must not be empty");
            if (Util.isEmpty(children)) {
                Assert.hasText(url, "'url' must not be empty");
            }
            this.label = label;
            this.url = url;
            this.order = order;
            this.iframe = iframe;
            this.children = children;
        }

    }

    @Data
    @JsonInclude(Include.NON_EMPTY)
    public static class ViewSettings {

        /**
         * Name of the view to address.
         */
        private final String name;

        /**
         * Set view enabled.
         */
        private boolean enabled;

        public ViewSettings(String name, boolean enabled) {
            Assert.hasText(name, "'name' must not be empty");
            this.name = name;
            this.enabled = enabled;
        }

    }

    @Data
    public static class LoginForm {

        private String username;

        private String password;
    }

}
