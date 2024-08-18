
package io.github.weimin96.manager.client.registration;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * @author pwm
 */
public class ReactiveRegistrationClient implements RegistrationClient {

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
	};

	private final WebClient webclient;

	private final Duration timeout;

	public ReactiveRegistrationClient(WebClient webclient, Duration timeout) {
		this.webclient = webclient;
		this.timeout = timeout;
	}

	@Override
	public String register(String serverUrl, Application application) {
		Map<String, Object> response = this.webclient.post().uri(serverUrl).headers(this::setRequestHeaders)
				.bodyValue(application).retrieve().bodyToMono(RESPONSE_TYPE).timeout(this.timeout).block();
		// TODO
		if (response.get("code") == HttpStatus.UNAUTHORIZED) {
			throw new IllegalStateException(HttpStatus.UNAUTHORIZED.name());
		}
		return response.get("id").toString();
	}

	@Override
	public void deregister(String serverUrl, String id) {
		this.webclient.delete().uri(serverUrl + '/' + id).retrieve().toBodilessEntity().timeout(this.timeout).block();
	}

	protected void setRequestHeaders(HttpHeaders headers) {
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	}

}
