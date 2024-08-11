
package io.github.weimin96.manager.server.web.client.cookies;

import io.github.weimin96.manager.server.domain.events.InstanceDeregisteredEvent;
import io.github.weimin96.manager.server.domain.events.InstanceEvent;
import io.github.weimin96.manager.server.services.AbstractEventHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CookieStoreCleanupTrigger extends AbstractEventHandler<InstanceDeregisteredEvent> {

	private final PerInstanceCookieStore cookieStore;

	public CookieStoreCleanupTrigger(final Publisher<InstanceEvent> publisher,
			final PerInstanceCookieStore cookieStore) {
		super(publisher, InstanceDeregisteredEvent.class);

		this.cookieStore = cookieStore;
	}

	@Override
	protected Publisher<Void> handle(final Flux<InstanceDeregisteredEvent> publisher) {
		return publisher.flatMap((event) -> {
			cleanupCookieStore(event);
			return Mono.empty();
		});
	}

	private void cleanupCookieStore(final InstanceDeregisteredEvent event) {
		cookieStore.cleanupInstance(event.getInstance());
	}

}
