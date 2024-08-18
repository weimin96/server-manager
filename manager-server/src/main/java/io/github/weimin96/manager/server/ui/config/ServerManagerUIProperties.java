
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
@ConfigurationProperties("manager.server.ui")
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
     * 系统图标
     */
    private String icon = "assets/img/icon-server-manager.svg";

    /**
     * favicon图标
     */
    private String favicon = "assets/img/favicon.png";

    /**
     * 通知图标
     */
    private String faviconDanger = "assets/img/favicon-danger.png";

    /**
     * 系统标题
     */
    private String title = "Server Manager";

    /**
     * 导航栏中徽标
     */
    private String brand = "<img src=\"assets/img/icon-server-manager.svg\"><span>Server Manager</span>";

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

    /**
     * 是否启用记住密码
     */
    private boolean rememberMeEnabled = true;

    private PollTimer pollTimer = new PollTimer();

    /**
     * 要从主页重定向过滤器中排除的其他路由。对这些的请求路由不会重定向到主页
     */
    private List<String> additionalRouteExcludes = new ArrayList<>();

    /**
     * 主题
     */
    private UiTheme theme = new UiTheme();

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

    @Data
    public static class UiTheme {

        private Boolean backgroundEnabled = true;

        private Palette palette = new Palette();

        private String color = "#14615A";

    }

    @Getter
    public static class Palette {

        private String shade50 = "#EEFCFA";

        private String shade100 = "#D9F7F4";

        private String shade200 = "#B7F0EA";

        private String shade300 = "#91E8E0";

        private String shade400 = "#6BE0D5";

        private String shade500 = "#47D9CB";

        private String shade600 = "#27BEAF";

        private String shade700 = "#1E9084";

        private String shade800 = "#14615A";

        private String shade900 = "#0A2F2B";

        public void set50(String shade50) {
            this.shade50 = shade50;
        }

        public void set100(String shade100) {
            this.shade100 = shade100;
        }

        public void set200(String shade200) {
            this.shade200 = shade200;
        }

        public void set300(String shade300) {
            this.shade300 = shade300;
        }

        public void set400(String shade400) {
            this.shade400 = shade400;
        }

        public void set500(String shade500) {
            this.shade500 = shade500;
        }

        public void set600(String shade600) {
            this.shade600 = shade600;
        }

        public void set700(String shade700) {
            this.shade700 = shade700;
        }

        public void set800(String shade800) {
            this.shade800 = shade800;
        }

        public void set900(String shade900) {
            this.shade900 = shade900;
        }

    }

}
