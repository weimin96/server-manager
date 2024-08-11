

package io.github.weimin96.manager.server.services.endpoints;

import io.github.weimin96.manager.server.domain.entity.Instance;
import io.github.weimin96.manager.server.domain.value.Endpoints;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

/**
 * @author weimin
 */
public class ChainingStrategy implements EndpointDetectionStrategy {

	private final EndpointDetectionStrategy[] delegates;

	public ChainingStrategy(EndpointDetectionStrategy... delegates) {
		Assert.notNull(delegates, "'delegates' must not be null.");
		Assert.noNullElements(delegates, "'delegates' must not contain null.");
		this.delegates = delegates;
	}

	@Override
	public Mono<Endpoints> detectEndpoints(Instance instance) {
		Mono<Endpoints> result = Mono.empty();
		for (EndpointDetectionStrategy delegate : delegates) {
			result = result.switchIfEmpty(delegate.detectEndpoints(instance));
		}
		return result.switchIfEmpty(Mono.just(Endpoints.empty()));
	}

}
