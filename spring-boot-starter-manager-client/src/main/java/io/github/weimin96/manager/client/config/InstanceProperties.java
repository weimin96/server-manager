

package io.github.weimin96.manager.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author pwm
 */
@lombok.Data
@ConfigurationProperties(prefix = "spring.boot.manager.client.instance")
public class InstanceProperties {

	/**
	 * Management-url to register with. Inferred at runtime, can be overridden in case the
	 * reachable URL is different (e.g. Docker).
	 */
	private String managementUrl;

	/**
	 * Base url for computing the management-url to register with. The path is inferred at
	 * runtime, and appended to the base url.
	 */
	private String managementBaseUrl;

	/**
	 * Client-service-URL register with. Inferred at runtime, can be overridden in case
	 * the reachable URL is different (e.g. Docker).
	 */
	private String serviceUrl;

	/**
	 * Base url for computing the service-url to register with. The path is inferred at
	 * runtime, and appended to the base url.
	 */
	private String serviceBaseUrl;

	/**
	 * Path for computing the service-url to register with. If not specified, defaults to
	 * "/"
	 */
	private String servicePath;

	/**
	 * Client-health-URL to register with. Inferred at runtime, can be overridden in case
	 * the reachable URL is different (e.g. Docker). Must be unique all services registry.
	 */
	private String healthUrl;

	/**
	 * 应用名称
	 */
	@Value("${spring.application.name:spring-boot-application}")
	private String name = "spring-boot-application";

	/**
	 * 如果注册的url是用服务器构建的。地址或主机名（IP, HOST_NAME, CANONICAL_HOST_NAME）。 默认IP
	 */
	private ServiceHostType serviceHostType = ServiceHostType.IP;

	/**
	 * 元数据
	 */
	private Map<String, String> metadata = new LinkedHashMap<>();

}
