
package io.github.weimin96.manager.client.registration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;

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

    private final String[] serverUrls;

    private final boolean registerOnce;

    private final RegistrationClient registrationClient;

    public DefaultApplicationRegistrator(ApplicationFactory applicationFactory, RegistrationClient registrationClient,
                                         String[] serverUrls, boolean registerOnce) {
        this.applicationFactory = applicationFactory;
        this.serverUrls = serverUrls;
        this.registerOnce = registerOnce;
        this.registrationClient = registrationClient;
    }

    /**
     * 注册客户端
     *
     * @return 成功或失败
     */
    @Override
    public boolean register() {
        Application application = this.applicationFactory.createApplication();
        boolean isRegistrationSuccessful = false;

        for (String serverUrl : this.serverUrls) {
            LongAdder attempt = this.attempts.computeIfAbsent(serverUrl, (k) -> new LongAdder());
            boolean successful = register(application, serverUrl, attempt.intValue() == 0);

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

    protected boolean register(Application application, String serverUrl, boolean firstAttempt) {
        try {
            String id = this.registrationClient.register(serverUrl, application);
            if (this.registeredId.compareAndSet(null, id)) {
                log.info("注册应用 {}", id);
            } else {
                log.debug("注册应用 {}", id);
            }
            return true;
        } catch (Exception ex) {
            if (firstAttempt) {
                if (ex instanceof IllegalStateException || (ex instanceof HttpClientErrorException && ((HttpClientErrorException) ex).getRawStatusCode() == 401)) {
                    log.error("注册失败，请检查授权信息配置 `spring.boot.manager.authority`");
                } else {
                    log.warn(
                            "注册失败 {} - ({}): {}. ",
                            application, this.serverUrls, ex.getMessage());
                }

            } else {
                log.debug("注册失败 {} - ({}): {}", application,
                        this.serverUrls, ex.getMessage());
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

        for (String serverUrl : this.serverUrls) {
            try {
                this.registrationClient.deregister(serverUrl, id);
                this.registeredId.compareAndSet(id, null);
                if (this.registerOnce) {
                    break;
                }
            } catch (Exception ex) {
                log.warn("注销应用失败 (id={}) - ({}): {}", id, serverUrl,
                        ex.getMessage());
            }
        }
    }

    @Override
    public String getRegisteredId() {
        return this.registeredId.get();
    }

}
