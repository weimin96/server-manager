
package io.github.weimin96.manager.server.ui.config;

import io.github.weimin96.manager.server.ui.web.UIController;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.http.CacheControl;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author pwm
 */
@Data
@ConfigurationProperties("spring.boot.manager.server.ui")
public class ServerManagerUIProperties {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/META-INF/server-ui/"};

    /**
     * ui文件路径
     */
    private String[] resourceLocations = CLASSPATH_RESOURCE_LOCATIONS;

    /**
     * 本地资源文件路径
     */
    private String templateLocation = CLASSPATH_RESOURCE_LOCATIONS[0];

    /**
     * 系统标题
     */
    private String title = "Server Manager";

    /**
     * 导航栏中徽标
     */
    private String brand = "<img style=\"width: auto;height: 45px\" src=\"assets/img/icon-server-manager.svg\">";

    /**
     * 反向代理路径
     */
    private String publicUrl = null;

    /**
     * 是否启用缓存
     */
    private boolean cacheTemplates = true;

    /**
     * 缓存信息
     */
    private Cache cache = new Cache();

    /**
     * 额外的导航栏视图
     */
    private List<UIController.ExternalView> externalViews = new ArrayList<>();

    /**
     * 额外的导航栏路由
     */
    private List<UIController.ViewSettings> viewSettings = new ArrayList<>();

    private PollTimer pollTimer = new PollTimer();

    /**
     * 要从主页重定向过滤器中排除的其他路由。对这些的请求路由不会重定向到主页
     */
    private List<String> additionalRouteExcludes = new ArrayList<>();

    @Data
    public static class PollTimer {

        /**
         * 轮询持续时间（以毫秒为单位），用于获取新的缓存数据.
         */
        private int cache = 2500;

        /**
         * 轮询持续时间（以毫秒为单位），用于获取新的数据源数据
         */
        private int datasource = 2500;

        /**
         * 轮询持续时间（以毫秒为单位），用于获取新的 GC 数据
         */
        private int gc = 2500;

        /**
         * 轮询持续时间（以毫秒为单位），用于获取新的进程数据
         */
        private int process = 2500;

        /**
         * 轮询持续时间（以毫秒为单位）以获取新的内存数据
         */
        private int memory = 2500;

        /**
         * 轮询持续时间（以毫秒为单位）以获取新线程数据
         */
        private int threads = 2500;

    }

    @Data
    public static class Cache {

        /**
         * 在Cache-Control http header中加入"max-age"指令。 默认3600
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration maxAge = Duration.ofSeconds(3600);

        /**
         * 是否在Cache-Control http header中加入"no-cache"指令。 默认false
         */
        private Boolean noCache = false;

        /**
         * 是否在Cache-Control http头文件中加入"no-store"指令。 默认false
         */
        private Boolean noStore = false;

        public CacheControl toCacheControl() {
            if (Boolean.TRUE.equals(this.noStore)) {
                return CacheControl.noStore();
            }
            if (Boolean.TRUE.equals(this.noCache)) {
                return CacheControl.noCache();
            }
            if (this.maxAge != null) {
                return CacheControl.maxAge(this.maxAge.getSeconds(), TimeUnit.SECONDS);
            }
            return CacheControl.empty();
        }

    }

}
