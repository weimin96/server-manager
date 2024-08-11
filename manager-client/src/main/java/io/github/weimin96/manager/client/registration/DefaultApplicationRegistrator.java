
package io.github.weimin96.manager.client.registration;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author pwm
 */
@Slf4j
public class DefaultApplicationRegistrator implements ApplicationRegistrator {

    private final ConcurrentHashMap<String, LongAdder> attempts = new ConcurrentHashMap<>();

    private final AtomicReference<String> registeredId = new AtomicReference<>();

    private final ApplicationFactory applicationFactory;

    private final String[] adminUrls;

    private final boolean registerOnce;

    private final RegistrationClient registrationClient;

    public DefaultApplicationRegistrator(ApplicationFactory applicationFactory, RegistrationClient registrationClient,
                                         String[] adminUrls, boolean registerOnce) {
        this.applicationFactory = applicationFactory;
        this.adminUrls = adminUrls;
        this.registerOnce = registerOnce;
        this.registrationClient = registrationClient;
    }

    /**
     * Registers the client application at spring-boot-admin-server.
     *
     * @return true if successful registration on at least one admin server
     */
    @Override
    public boolean register() {
        Application application = this.applicationFactory.createApplication();
        boolean isRegistrationSuccessful = false;

        for (String adminUrl : this.adminUrls) {
            LongAdder attempt = this.attempts.computeIfAbsent(adminUrl, (k) -> new LongAdder());
            boolean successful = register(application, adminUrl, attempt.intValue() == 0);

            if (!successful) {
                attempt.increment();
            } else {
                attempt.reset();
                isRegistrationSuccessful = true;
                if (this.registerOnce) {
                    break;
                }
            }
        }

        return isRegistrationSuccessful;
    }

    protected boolean register(Application application, String adminUrl, boolean firstAttempt) {
        try {
            String id = this.registrationClient.register(adminUrl, application);
            if (this.registeredId.compareAndSet(null, id)) {
                log.info("Application registered itself as {}", id);
            } else {
                log.debug("Application refreshed itself as {}", id);
            }
            return true;
        } catch (Exception ex) {
            if (firstAttempt) {
                log.warn(
                        "Failed to register application as {} at spring-boot-admin ({}): {}. Further attempts are logged on DEBUG level",
                        application, this.adminUrls, ex.getMessage());
            } else {
                log.debug("Failed to register application as {} at spring-boot-admin ({}): {}", application,
                        this.adminUrls, ex.getMessage());
            }
            return false;
        }
    }

    @Override
    public void deregister() {
        String id = this.registeredId.get();
        if (id == null) {
            return;
        }

        for (String adminUrl : this.adminUrls) {
            try {
                this.registrationClient.deregister(adminUrl, id);
                this.registeredId.compareAndSet(id, null);
                if (this.registerOnce) {
                    break;
                }
            } catch (Exception ex) {
                log.warn("Failed to deregister application (id={}) at spring-boot-admin ({}): {}", id, adminUrl,
                        ex.getMessage());
            }
        }
    }

    @Override
    public String getRegisteredId() {
        return this.registeredId.get();
    }

}
