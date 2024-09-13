package io.github.weimin96.manager.server.config;

import io.github.weimin96.manager.server.utils.Util;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * server 配置类
 *
 * @author panwm
 * @since 2024/8/2 22:30
 */
@Data
@ConfigurationProperties(prefix = "spring.boot.manager.server")
public class ServerManagerProperties {

    /**
     * server服务地址上下文 默认：serverManager
     */
    private String contextPath = "serverManager";

    /**
     * 服务信息配置
     */
    private ServerProperties server = new ServerProperties();

    /**
     * 监控信息
     */
    private MonitorProperties monitor = new MonitorProperties();

    /**
     * 请求转发配置
     */
    private InstanceProxyProperties instanceProxy = new InstanceProxyProperties();

    /**
     * 授权信息
     */
    private InstanceAuthProperties authority = new InstanceAuthProperties();

    private String[] probedEndpoints = {"health", "env", "metrics", "httptrace:trace", "httptrace", "threaddump:dump",
            "threaddump", "jolokia", "info", "logfile", "logdir", "logcontent", "refresh", "liquibase", "heapdump", "loggers",
            "auditevents", "mappings", "scheduledtasks", "configprops", "caches", "beans"};

    public void setContextPath(String contextPath) {
        this.contextPath = Util.normalizePath(contextPath);
    }

    public String path(String path) {
        return this.contextPath + path;
    }

    @Data
    public static class ServerProperties {

        /**
         * 是否启用服务: 默认true
         */
        private boolean enabled = true;

        /**
         * 状态上报时间间隔 : 默认10秒
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration statusInterval = Duration.ofMillis(10_000L);

        /**
         * 状态存活时间 : 默认10秒
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration statusLifetime = Duration.ofMillis(10_000L);

        /**
         * 信息上报时间间隔 : 默认1分钟
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration infoInterval = Duration.ofMinutes(1L);

        /**
         * 信息存活时间 : 默认1分钟
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration infoLifetime = Duration.ofMinutes(1L);

        /**
         * 默认超时时间 : 默认10秒
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration defaultTimeout = Duration.ofMillis(10_000L);

    }

    @Data
    public static class MonitorProperties {

        /**
         * 实例状态检查的时间间隔。
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration statusInterval = Duration.ofMillis(10_000L);

        /**
         * 实例状态存活时间。
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration statusLifetime = Duration.ofMillis(10_000L);

        /**
         * 信息上报时间间隔。
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration infoInterval = Duration.ofMinutes(1L);

        /**
         * 信息存活时间。
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration infoLifetime = Duration.ofMinutes(1L);

        /**
         * 默认重试次数。
         */
        private int defaultRetries = 0;

        /**
         * 重试次数。
         */
        private Map<String, Integer> retries = new HashMap<>();

        /**
         * 默认超时时间。
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Duration defaultTimeout = Duration.ofMillis(10_000L);

        /**
         * 超时时间。
         */
        @DurationUnit(ChronoUnit.MILLIS)
        private Map<String, Duration> timeout = new HashMap<>();

    }

    @Data
    public static class InstanceProxyProperties {

        /**
         * 忽略的请求头
         */
        private Set<String> ignoredHeaders = new HashSet<>(asList("Cookie", "Set-Cookie", "Authorization"));

    }

    @Data
    public static class InstanceAuthProperties {

        /**
         * 是否启用登录验证 默认启用
         */
        private boolean enabled = false;

        /**
         * 登录验证用户名
         */
        private String defaultUserName = null;

        /**
         * 登录验证密码
         */
        private String defaultPassword = null;

    }
}
