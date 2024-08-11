
package io.github.weimin96.manager.server.web.client;

import io.github.weimin96.manager.server.domain.entity.Instance;
import org.springframework.http.HttpHeaders;

import java.util.Collection;

/**
 * @author pwm
 */
public class CompositeHttpHeadersProvider implements HttpHeadersProvider {

	private final Collection<HttpHeadersProvider> delegates;

	public CompositeHttpHeadersProvider(Collection<HttpHeadersProvider> delegates) {
		this.delegates = delegates;
	}

	@Override
	public HttpHeaders getHeaders(Instance instance) {
		HttpHeaders headers = new HttpHeaders();
		delegates.forEach((delegate) -> headers.addAll(delegate.getHeaders(instance)));
		return headers;
	}

}
