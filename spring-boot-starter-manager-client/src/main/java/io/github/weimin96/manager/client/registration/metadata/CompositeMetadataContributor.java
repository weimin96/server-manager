
package io.github.weimin96.manager.client.registration.metadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pwm
 */
public class CompositeMetadataContributor implements MetadataContributor {

	private final List<MetadataContributor> delegates;

	public CompositeMetadataContributor(List<MetadataContributor> delegates) {
		this.delegates = delegates;
	}

	@Override
	public Map<String, String> getMetadata() {
		Map<String, String> metadata = new LinkedHashMap<>();
		delegates.forEach((delegate) -> metadata.putAll(delegate.getMetadata()));
		return metadata;
	}

}
