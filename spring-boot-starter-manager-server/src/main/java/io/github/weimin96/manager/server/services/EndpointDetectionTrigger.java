
package io.github.weimin96.manager.server.services;

import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import io.github.weimin96.manager.server.domain.events.InstanceRegistrationUpdatedEvent;
import io.github.weimin96.manager.server.domain.events.InstanceStatusChangedEvent;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author pwm
 */
public class EndpointDetectionTrigger extends AbstractEventHandler<InstanceEvent> {

	private static final Logger log = LoggerFactory.getLogger(EndpointDetectionTrigger.class);

	private final EndpointDetector endpointDetector;

	public EndpointDetectionTrigger(EndpointDetector endpointDetector, Publisher<InstanceEvent> publisher) {
		super(publisher, InstanceEvent.class);
		this.endpointDetector = endpointDetector;
	}

	@Override
	protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {
		return publisher.filter((event) -> event instanceof InstanceStatusChangedEvent
				|| event instanceof InstanceRegistrationUpdatedEvent).flatMap(this::detectEndpoints);
	}

	protected Mono<Void> detectEndpoints(InstanceEvent event) {
		return this.endpointDetector.detectEndpoints(event.getInstance()).onErrorResume((e) -> {
			log.warn("Unexpected error while detecting endpoints for {}", event.getInstance(), e);
			return Mono.empty();
		});
	}

}
