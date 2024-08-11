
package io.github.weimin96.manager.server.web.client;

import io.github.weimin96.manager.server.domain.entity.Instance;
import org.springframework.http.HttpHeaders;


/**
 * @author pwm
 */
public interface HttpHeadersProvider {

	HttpHeaders getHeaders(Instance instance);

}
