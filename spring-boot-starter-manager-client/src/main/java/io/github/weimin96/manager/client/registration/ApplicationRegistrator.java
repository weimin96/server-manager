
package io.github.weimin96.manager.client.registration;

/**
 *
 * @author pwm
 */
public interface ApplicationRegistrator {

	/**
	 * 注册
	 * @return boolean
	 */
	boolean register() throws IllegalStateException;

	/**
	 * 注销
	 */
	void deregister();

	/**
	 * 获取注册的id
	 * @return 注册的id
	 */
	String getRegisteredId();

}
