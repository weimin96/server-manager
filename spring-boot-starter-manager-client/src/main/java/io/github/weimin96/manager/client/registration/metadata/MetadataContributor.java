
package io.github.weimin96.manager.client.registration.metadata;

import java.util.Map;

/**
 * @author pwm
 */
@FunctionalInterface
public interface MetadataContributor {

	Map<String, String> getMetadata();

}
