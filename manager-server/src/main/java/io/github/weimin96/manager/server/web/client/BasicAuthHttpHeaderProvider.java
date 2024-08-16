
package io.github.weimin96.manager.server.web.client;

import io.github.weimin96.manager.server.domain.entity.Instance;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * @author panwm
 */
public class BasicAuthHttpHeaderProvider implements HttpHeadersProvider {

    private static final String[] USERNAME_KEYS = {"user.name", "user-name", "username"};

    private static final String[] PASSWORD_KEYS = {"user.password", "user-password", "userpassword"};

    private final String defaultUserName;

    private final String defaultPassword;

    public BasicAuthHttpHeaderProvider(String defaultUserName, String defaultPassword) {
        this.defaultUserName = defaultUserName;
        this.defaultPassword = defaultPassword;
    }

    public BasicAuthHttpHeaderProvider() {
        this(null, null);
    }

    @Override
    public HttpHeaders getHeaders(Instance instance) {
        String username = getMetadataValue(instance, USERNAME_KEYS);
        String password = getMetadataValue(instance, PASSWORD_KEYS);

		if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
			username = this.defaultUserName;
			password = this.defaultPassword;
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

}
