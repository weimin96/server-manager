package io.github.weimin96.manager.client.config.druid;

import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import io.github.weimin96.manager.client.config.ClientManagerAutoConfiguration;
import io.github.weimin96.manager.client.endpoints.druid.DruidStateEndpoint;
import io.github.weimin96.manager.client.registration.ApplicationRegistrator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author panwm
 * @since 2024/9/11 23:08
 */
@AutoConfiguration(after = ClientManagerAutoConfiguration.class)
@ConditionalOnBean({DruidStatProperties.class, ApplicationRegistrator.class})
public class ClientManagerDruidConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DruidStateEndpoint druidStateEndpoint() {
        return new DruidStateEndpoint();
    }
}
