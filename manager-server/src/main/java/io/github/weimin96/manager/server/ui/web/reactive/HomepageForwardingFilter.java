
package io.github.weimin96.manager.server.ui.web.reactive;

import io.github.weimin96.manager.server.ui.web.HomepageForwardingFilterConfig;
import io.github.weimin96.manager.server.ui.web.HomepageForwardingMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author pwm
 */
public class HomepageForwardingFilter implements WebFilter {

	private static final Logger log = LoggerFactory.getLogger(HomepageForwardingFilter.class);

	private final String homepage;

	private final HomepageForwardingMatcher<ServerHttpRequest> matcher;

	public HomepageForwardingFilter(String homepage,List<String> routeExcludes) {
		this.homepage = homepage;
		this.matcher = new HomepageForwardingMatcher<>(routeExcludes, ServerHttpRequest::getMethodValue,
				(r) -> r.getPath().pathWithinApplication().toString(), (r) -> r.getHeaders().getAccept());
	}

	public HomepageForwardingFilter(HomepageForwardingFilterConfig filterConfig) {
		this(filterConfig.getHomepage(), filterConfig.getRoutesExcludes());
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		if (this.matcher.test(exchange.getRequest())) {
			log.trace("Forwarding request with URL {} to index", exchange.getRequest().getURI());
			exchange = exchange.mutate().request((request) -> request.path(this.homepage)).build();
		}
		return chain.filter(exchange);
	}

}
