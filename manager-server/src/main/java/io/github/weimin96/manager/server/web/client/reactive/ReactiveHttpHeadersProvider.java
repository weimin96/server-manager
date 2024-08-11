
package io.github.weimin96.manager.server.web.client.reactive;

import io.github.weimin96.manager.server.domain.entity.Instance;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;


public interface ReactiveHttpHeadersProvider {

	Mono<HttpHeaders> getHeaders(Instance instance);

}
