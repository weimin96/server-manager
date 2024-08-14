
package io.github.weimin96.manager.server.ui.config;

import io.github.weimin96.manager.server.ui.web.UiController;
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
@lombok.Data
@ConfigurationProperties("manager.server.ui")
public class AdminServerUiProperties {

	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/META-INF/server-ui/" };

	/**
	 * ui文件路径
	 */
	private String[] resourceLocations = CLASSPATH_RESOURCE_LOCATIONS;

	/**
	 * Locations of SBA ui template.
	 */
	private String templateLocation = CLASSPATH_RESOURCE_LOCATIONS[0];

	/**
	 * 登录页面图标
	 */
	private String loginIcon = "assets/img/icon-server-manager.svg";

	/**
	 * 默认图标和桌面通知图标的图标
	 */
	private String favicon = "assets/img/favicon.png";

	/**
	 * 当一项或多项服务关闭时用作图标的图标，以及用于桌面通知的图标
	 */
	private String faviconDanger = "assets/img/favicon-danger.png";

	/**
	 * 页面标题
	 */
	private String title = "Server Manager";

	/**
	 * 导航栏中显示的品牌
	 */
	private String brand = "<img src=\"assets/img/icon-server-manager.svg\"><span>Server Manager</span>";

	/**
	 * If running behind a reverse proxy (using path rewriting) this can be used to output
	 * correct self references. If the host/port is omitted it will be inferred from the
	 * request.
	 */
	private String publicUrl = null;

	/**
	 * Wether the thymeleaf templates should be cached.
	 */
	private boolean cacheTemplates = true;

	/**
	 * Cache-Http-Header settings.
	 */
	private Cache cache = new Cache();

	/**
	 * External views shown in the navbar.
	 */
	private List<UiController.ExternalView> externalViews = new ArrayList<>();

	/**
	 * External views shown in the navbar.
	 */
	private List<UiController.ViewSettings> viewSettings = new ArrayList<>();

	/**
	 * Whether the option to remember a user should be available.
	 */
	private boolean rememberMeEnabled = true;

	/**
	 * Limit languages to this list. Intersection of all supported languages and this list
	 * will be used.
	 */
	private List<String> availableLanguages = new ArrayList<>();

	private PollTimer pollTimer = new PollTimer();

	/**
	 * Additional routes to exclude from home page redirecting filter. Requests to these
	 * routes will not redirected to home page
	 */
	private List<String> additionalRouteExcludes = new ArrayList<>();

	/**
	 * 允许启用 Toast 通知
	 */
	private Boolean enableToasts = false;

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
		 * include "max-age" directive in Cache-Control http header.
		 */
		@DurationUnit(ChronoUnit.SECONDS)
		private Duration maxAge = Duration.ofSeconds(3600);

		/**
		 * include "no-cache" directive in Cache-Control http header.
		 */
		private Boolean noCache = false;

		/**
		 * include "no-store" directive in Cache-Control http header.
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
