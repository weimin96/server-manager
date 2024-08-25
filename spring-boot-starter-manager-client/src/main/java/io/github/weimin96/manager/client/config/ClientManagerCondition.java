
package io.github.weimin96.manager.client.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 客户端启动条件
 * @author pwm
 */
@Slf4j
public class ClientManagerCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata annotatedTypeMetadata) {
		ClientProperties clientProperties = getClientProperties(context);

		if (!clientProperties.isEnabled()) {
			log.info("Spring Boot Manager Client is disabled, because 'spring.boot.manager.client.enabled' is false.");
			return ConditionOutcome
					.noMatch("Spring Boot Manager Client is disabled, because 'spring.boot.manager.client.enabled' is false.");
		}

		if (clientProperties.getUrl().length == 0 && !StringUtils.hasText(clientProperties.getServerApplicationName())) {
			log.info("Spring Boot Manager Client is disabled, because 'spring.boot.manager.client.url' and 'spring.boot.manager.client.server-application-name' is empty.");
			return ConditionOutcome
					.noMatch("Spring Boot Manager Client is disabled, because 'spring.boot.manager.client.url' and 'spring.boot.manager.client.server-application-name' is empty.");
		}
		return ConditionOutcome.match();
	}

	private ClientProperties getClientProperties(ConditionContext context) {
		ClientProperties clientProperties = new ClientProperties();
		Binder.get(context.getEnvironment()).bind("spring.boot.manager.client", Bindable.ofInstance(clientProperties));
		return clientProperties;
	}

}
