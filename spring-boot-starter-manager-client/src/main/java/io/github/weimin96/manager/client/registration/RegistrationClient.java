
package io.github.weimin96.manager.client.registration;

/**
 * @author pwm
 */
public interface RegistrationClient {

	String register(String serverUrl, Application self);

	void deregister(String serverUrl, String id);

}
