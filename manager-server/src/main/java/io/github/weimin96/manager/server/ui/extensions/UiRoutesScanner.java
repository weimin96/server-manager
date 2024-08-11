
package io.github.weimin96.manager.server.ui.extensions;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pwm
 */
@Slf4j
public class UiRoutesScanner {

	private final ResourcePatternResolver resolver;

	public UiRoutesScanner(ResourcePatternResolver resolver) {
		this.resolver = resolver;
	}

	public List<String> scan(String... locations) throws IOException {
		List<String> routes = new ArrayList<>();
		for (String location : locations) {
			for (Resource resource : this.resolver.getResources(toPattern(location) + "**/routes.txt")) {
				if (resource.isReadable()) {
					routes.addAll(readLines(resource.getInputStream()));
				}
			}
		}
		return routes;
	}

	private List<String> readLines(InputStream input) {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
			return buffer.lines().map(String::trim).filter(StringUtils::hasText).collect(Collectors.toList());
		}
		catch (IOException ex) {
			log.warn("Couldn't read routes from", ex);
			return Collections.emptyList();
		}
	}

	private String toPattern(String location) {
		// replace the classpath pattern to search all locations and not just the first
		return location.replace("classpath:", "classpath*:");
	}

}
