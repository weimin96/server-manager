package io.github.weimin96.manager.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author panwm
 * @since 2024/8/2 22:50
 */
public class ServerManagerCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ServerManagerProperties properties = getProperties(context);
        if (!properties.getServer().isEnabled()) {
            return ConditionOutcome
                    .noMatch("'manager.admin.enabled' is false.");
        }
        return ConditionOutcome.match();
    }

    private ServerManagerProperties getProperties(ConditionContext context) {
        ServerManagerProperties properties = new ServerManagerProperties();
        Binder.get(context.getEnvironment()).bind("manager.admin", Bindable.ofInstance(properties));
        return properties;
    }
}
