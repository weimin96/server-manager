package io.github.weimin96.manager.server.eventstore;

/**
 * @author panwm
 * @since 2024/8/3 21:26
 */
public class OptimisticLockingException extends RuntimeException {

    public OptimisticLockingException(String message) {
        super(message);
    }
}
