
package io.github.weimin96.manager.client.registration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;


/**
 * @author pwm
 */
@Slf4j
public class RegistrationApplicationListener implements InitializingBean, DisposableBean {

	private final ApplicationRegistrator registrator;

	private final ThreadPoolTaskScheduler taskScheduler;

	private boolean autoDeregister = false;

	private boolean autoRegister = true;

	private Duration registerPeriod = Duration.ofSeconds(10);

	private volatile ScheduledFuture<?> scheduledTask;

	public RegistrationApplicationListener(ApplicationRegistrator registrator) {
		this(registrator, registrationTaskScheduler());
	}

	private static ThreadPoolTaskScheduler registrationTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.setRemoveOnCancelPolicy(true);
		taskScheduler.setThreadNamePrefix("registrationTask");
		return taskScheduler;
	}

	RegistrationApplicationListener(ApplicationRegistrator registrator, ThreadPoolTaskScheduler taskScheduler) {
		this.registrator = registrator;
		this.taskScheduler = taskScheduler;
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onApplicationReady(ApplicationReadyEvent event) {
		if (autoRegister) {
			startRegisterTask();
		}
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onClosedContext(ContextClosedEvent event) {
		if (event.getApplicationContext().getParent() == null
				|| "bootstrap".equals(event.getApplicationContext().getParent().getId())) {
			stopRegisterTask();

			if (autoDeregister) {
				registrator.deregister();
			}
		}
	}

	public void startRegisterTask() {
		if (scheduledTask != null && !scheduledTask.isDone()) {
			return;
		}

		scheduledTask = taskScheduler.scheduleAtFixedRate(registrator::register, registerPeriod);
		log.debug("Scheduled registration task for every {}ms", registerPeriod.toMillis());
	}

	public void stopRegisterTask() {
		if (scheduledTask != null && !scheduledTask.isDone()) {
			scheduledTask.cancel(true);
			log.debug("停止注册任务");
		}
	}

	public void setAutoDeregister(boolean autoDeregister) {
		this.autoDeregister = autoDeregister;
	}

	public void setAutoRegister(boolean autoRegister) {
		this.autoRegister = autoRegister;
	}

	public void setRegisterPeriod(Duration registerPeriod) {
		this.registerPeriod = registerPeriod;
	}

	@Override
	public void afterPropertiesSet() {
		taskScheduler.afterPropertiesSet();
	}

	@Override
	public void destroy() {
		taskScheduler.destroy();
	}

}
