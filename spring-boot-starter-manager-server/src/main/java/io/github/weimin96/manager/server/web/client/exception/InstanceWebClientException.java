package io.github.weimin96.manager.server.web.client.exception;

/**
 * @author panwm
 * @since 2024/8/4 23:24
 */
public class InstanceWebClientException extends RuntimeException {

    public InstanceWebClientException(String message) {
        super(message);
    }

    public InstanceWebClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
