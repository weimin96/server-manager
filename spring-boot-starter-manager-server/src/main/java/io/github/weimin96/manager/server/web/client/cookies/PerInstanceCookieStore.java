
package io.github.weimin96.manager.server.web.client.cookies;

import io.github.weimin96.manager.server.domain.value.InstanceId;
import org.springframework.util.MultiValueMap;

import java.net.URI;


public interface PerInstanceCookieStore {


	MultiValueMap<String, String> get(InstanceId instanceId, URI requestUri,
									  MultiValueMap<String, String> requestHeaders);

	void put(InstanceId instanceId, URI requestUri, MultiValueMap<String, String> responseHeaders);

	void cleanupInstance(InstanceId instanceId);

}
