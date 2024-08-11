
package io.github.weimin96.manager.client.registration;

/**
 * @author pwm
 */
public interface RegistrationClient {

	String register(String adminUrl, Application self);

	void deregister(String adminUrl, String id);

}
