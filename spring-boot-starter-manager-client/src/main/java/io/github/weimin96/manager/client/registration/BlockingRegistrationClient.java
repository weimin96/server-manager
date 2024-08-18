
package io.github.weimin96.manager.client.registration;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * @author pwm
 */
public class BlockingRegistrationClient implements RegistrationClient {

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
	};

	private final RestTemplate restTemplate;

	public BlockingRegistrationClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public String register(String serverUrl, Application application) {
		ResponseEntity<Map<String, Object>> response = this.restTemplate.exchange(serverUrl, HttpMethod.POST,
				new HttpEntity<>(application, this.createRequestHeaders()), RESPONSE_TYPE);
		if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
			throw new IllegalStateException(String.valueOf(response.getStatusCode()));
		}
		return response.getBody().get("id").toString();
	}

	@Override
	public void deregister(String serverUi, String id) {
		this.restTemplate.delete(serverUi + '/' + id);
	}

	protected HttpHeaders createRequestHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		return HttpHeaders.readOnlyHttpHeaders(headers);
	}

}
