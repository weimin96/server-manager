
package io.github.weimin96.manager.server.web.client.cookies;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import io.github.weimin96.manager.server.web.client.exception.InstanceWebClientException;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class JdkPerInstanceCookieStore implements PerInstanceCookieStore {

	private static final String REQ_COOKIE_HEADER_KEY = "Cookie";

	private final Map<InstanceId, CookieHandler> cookieHandlerRegistry = new ConcurrentHashMap<>();

	private final CookiePolicy cookiePolicy;

	public JdkPerInstanceCookieStore() {
		this(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
	}

	public JdkPerInstanceCookieStore(final CookiePolicy cookiePolicy) {
		Assert.notNull(cookiePolicy, "'cookiePolicy' must not be null");
		this.cookiePolicy = cookiePolicy;
	}

	@Override
	public MultiValueMap<String, String> get(final InstanceId instanceId, final URI requestUri,
			final MultiValueMap<String, String> requestHeaders) {
		try {
			final List<String> rawCookies = getCookieHandler(instanceId).get(requestUri, requestHeaders)
					.get(REQ_COOKIE_HEADER_KEY);

			// split each rawCookie at first '=' into name/cookieValue and
			// return as MultiValueMap
			return Optional.ofNullable(rawCookies)
					.map((rcList) -> rcList.stream().map((rc) -> rc.split("=", 2)).collect(
							LinkedMultiValueMap<String, String>::new, (map, nv) -> map.add(nv[0], nv[1]),
							(m1, m2) -> m1.addAll(m2)))
					.orElseGet(LinkedMultiValueMap<String, String>::new);
		}
		catch (IOException ioe) {
			throw new InstanceWebClientException("Could not get cookies from store.", ioe);
		}
	}

	@Override
	public void put(final InstanceId instanceId, final URI requestUrl, final MultiValueMap<String, String> headers) {
		try {
			getCookieHandler(instanceId).put(requestUrl, headers);
		}
		catch (IOException ioe) {
			throw new InstanceWebClientException("Could not set cookies to store.", ioe);
		}
	}

	@Override
	public void cleanupInstance(final InstanceId instanceId) {
		cookieHandlerRegistry.computeIfPresent(instanceId, (id, ch) -> null);
	}

	protected CookieHandler getCookieHandler(final InstanceId instanceId) {
		return cookieHandlerRegistry.computeIfAbsent(instanceId, this::createCookieHandler);
	}

	protected CookieHandler createCookieHandler(final InstanceId instanceId) {
		return new CookieManager(null, cookiePolicy);
	}

}
