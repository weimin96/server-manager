
package io.github.weimin96.manager.server.ui.extensions;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@lombok.Data
public class UiExtensions implements Iterable<UiExtension> {

	public static final UiExtensions EMPTY = new UiExtensions(Collections.emptyList());

	private final List<UiExtension> extensions;

	public UiExtensions(List<UiExtension> extensions) {
		this.extensions = Collections.unmodifiableList(extensions);
	}

	@Override
	public Iterator<UiExtension> iterator() {
		return this.extensions.iterator();
	}

	public List<UiExtension> getCssExtensions() {
		return this.extensions.stream().filter((e) -> e.getResourcePath().endsWith(".css"))
				.collect(Collectors.toList());
	}

	public List<UiExtension> getJsExtensions() {
		return this.extensions.stream().filter((e) -> e.getResourcePath().endsWith(".js")).collect(Collectors.toList());
	}

}
