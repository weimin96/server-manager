
package io.github.weimin96.manager.server.web.client;

import io.github.weimin96.manager.server.domain.entity.Instance;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;


public class BasicAuthHttpHeaderProvider implements HttpHeadersProvider {

	private static final String[] USERNAME_KEYS = { "user.name", "user-name", "username" };

	private static final String[] PASSWORD_KEYS = { "user.password", "user-password", "userpassword" };

	private final String defaultUserName;

	private final String defaultPassword;

	private final Map<String, InstanceCredentials> serviceMap;

	public BasicAuthHttpHeaderProvider(String defaultUserName, String defaultPassword,
                                       Map<String, InstanceCredentials> serviceMap) {
		this.defaultUserName = defaultUserName;
		this.defaultPassword = defaultPassword;
		this.serviceMap = serviceMap;
	}

	public BasicAuthHttpHeaderProvider() {
		this(null, null, Collections.emptyMap());
	}

	@Override
	public HttpHeaders getHeaders(Instance instance) {
		String username = getMetadataValue(instance, USERNAME_KEYS);
		String password = getMetadataValue(instance, PASSWORD_KEYS);

		if (!(StringUtils.hasText(username) && StringUtils.hasText(password))) {
			String registeredName = instance.getRegistration().getName();
			InstanceCredentials credentials = this.serviceMap.get(registeredName);
			if (credentials != null) {
				username = credentials.getUserName();
				password = credentials.getUserPassword();
			}
			else {
				username = this.defaultUserName;
				password = this.defaultPassword;
			}
		}

		HttpHeaders headers = new HttpHeaders();
		if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
			headers.set(HttpHeaders.AUTHORIZATION, encode(username, password));
		}
		return headers;
	}

	protected String encode(String username, String password) {
		String token = Base64Utils.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		return "Basic " + token;
	}

	private static String getMetadataValue(Instance instance, String[] keys) {
		Map<String, String> metadata = instance.getRegistration().getMetadata();
		for (String key : keys) {
			String value = metadata.get(key);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	@lombok.Data
	@lombok.NoArgsConstructor
	@lombok.AllArgsConstructor
	public static class InstanceCredentials {

		/**
		 * user name for this instance
		 */
		@lombok.NonNull
		private String userName;

		/**
		 * user password for this instance
		 */
		@lombok.NonNull
		private String userPassword;

	}

}
