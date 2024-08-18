
package io.github.weimin96.manager.server.web.client.reactive;

import io.github.weimin96.manager.server.domain.entity.Instance;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class CompositeReactiveHttpHeadersProvider implements ReactiveHttpHeadersProvider {

	private final Collection<ReactiveHttpHeadersProvider> delegates;

	public CompositeReactiveHttpHeadersProvider(Collection<ReactiveHttpHeadersProvider> delegates) {
		this.delegates = delegates;
	}

	@Override
	public Mono<HttpHeaders> getHeaders(Instance instance) {
		List<Mono<HttpHeaders>> headers = delegates.stream()
				.map((reactiveHttpHeadersProvider) -> reactiveHttpHeadersProvider.getHeaders(instance))
				.collect(toList());

		return Mono.zip(headers, this::mergeMonosToHeaders);
	}

	private HttpHeaders mergeMonosToHeaders(Object[] e) {
		return Arrays.stream(e).map(HttpHeaders.class::cast).reduce(new HttpHeaders(), (h1, h2) -> {
			h1.addAll(h2);
			return h1;
		});
	}

}
