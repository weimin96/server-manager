
package io.github.weimin96.manager.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * This condition checks if the client should be enabled. Two properties are checked:
 * spring.boot.admin.client.enabled and spring.boot.admin.client.url. The following table
 * shows under which conditions the client is active. <pre>
 *           | enabled: false | enabled: true (default) |
 * --------- | -------------- | ----------------------- |
 * url empty | inactive       | inactive                |
 * (default) |                |                         |
 * --------- | -------------- | ----------------------- |
 * url set   | inactive       | active                  |
 * </pre>
 * @author pwm
 */
public class ClientManagerCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata annotatedTypeMetadata) {
		ClientProperties clientProperties = getClientProperties(context);

		if (!clientProperties.isEnabled()) {
			return ConditionOutcome
					.noMatch("Spring Boot Client is disabled, because 'manager.client.enabled' is false.");
		}

		if (clientProperties.getUrl().length == 0) {
			return ConditionOutcome
					.noMatch("Spring Boot Client is disabled, because 'manager.client.url' is empty.");
		}

		return ConditionOutcome.match();
	}

	private ClientProperties getClientProperties(ConditionContext context) {
		ClientProperties clientProperties = new ClientProperties();
		Binder.get(context.getEnvironment()).bind("manager.client", Bindable.ofInstance(clientProperties));
		return clientProperties;
	}

}
