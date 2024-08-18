
package io.github.weimin96.manager.client.registration;

import io.github.weimin96.manager.client.config.InstanceProperties;
import io.github.weimin96.manager.client.registration.metadata.MetadataContributor;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.web.server.Ssl;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletContext;

/**
 *
 * @author pwm
 */
public class ServletApplicationFactory extends DefaultApplicationFactory {

	private final ServletContext servletContext;

	private final ServerProperties server;

	private final ManagementServerProperties management;

	private final InstanceProperties instance;

	private final DispatcherServletPath dispatcherServletPath;

	public ServletApplicationFactory(InstanceProperties instance, ManagementServerProperties management,
			ServerProperties server, ServletContext servletContext, PathMappedEndpoints pathMappedEndpoints,
			WebEndpointProperties webEndpoint, MetadataContributor metadataContributor,
			DispatcherServletPath dispatcherServletPath) {
		super(instance, management, server, pathMappedEndpoints, webEndpoint, metadataContributor);
		this.servletContext = servletContext;
		this.server = server;
		this.management = management;
		this.instance = instance;
		this.dispatcherServletPath = dispatcherServletPath;
	}

	@Override
	protected String getServiceUrl() {
		if (instance.getServiceUrl() != null) {
			return instance.getServiceUrl();
		}

		return UriComponentsBuilder.fromUriString(getServiceBaseUrl()).path(getServicePath())
				.path(getServerContextPath()).toUriString();
	}

	@Override
	protected String getManagementBaseUrl() {
		String baseUrl = instance.getManagementBaseUrl();

		if (StringUtils.hasText(baseUrl)) {
			return baseUrl;
		}

		if (isManagementPortEqual()) {
			return UriComponentsBuilder.fromHttpUrl(getServiceUrl()).path("/").path(getDispatcherServletPrefix())
					.path(getManagementContextPath()).toUriString();
		}

		Ssl ssl = (management.getSsl() != null) ? management.getSsl() : server.getSsl();
		return UriComponentsBuilder.newInstance().scheme(getScheme(ssl)).host(getManagementHost())
				.port(getLocalManagementPort()).path(getManagementContextPath()).toUriString();
	}

	protected String getManagementContextPath() {
		return management.getBasePath();
	}

	protected String getServerContextPath() {
		return servletContext.getContextPath();
	}

	protected String getDispatcherServletPrefix() {
		return this.dispatcherServletPath.getPrefix();
	}

}
