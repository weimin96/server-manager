
package io.github.weimin96.manager.server.ui.web;

import lombok.Value;

import java.util.List;

/**
 * @author pwm
 */
@Value
public class HomepageForwardingFilterConfig {

	String homepage;

	List<String> routesIncludes;

	/**
	 * routes which are excluded intentionally (for instance downloads)
	 */
	List<String> routesExcludes;

}
