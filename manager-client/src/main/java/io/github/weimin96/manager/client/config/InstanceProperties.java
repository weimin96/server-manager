

package io.github.weimin96.manager.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author pwm
 */
@lombok.Data
@ConfigurationProperties(prefix = "manager.client.instance")
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
	 * Name to register with. Defaults to ${spring.application.name}
	 */
	@Value("${spring.application.name:spring-boot-application}")
	private String name = "spring-boot-application";

	/**
	 * Should the registered urls be built with server.address or with hostname.
	 */
	private ServiceHostType serviceHostType = ServiceHostType.CANONICAL_HOST_NAME;

	/**
	 * Metadata that should be associated with this application
	 */
	private Map<String, String> metadata = new LinkedHashMap<>();

}
