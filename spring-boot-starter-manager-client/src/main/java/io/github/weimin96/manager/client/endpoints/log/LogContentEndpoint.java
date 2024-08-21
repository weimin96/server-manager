package io.github.weimin96.manager.client.endpoints.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author panwm
 * @since 2024/8/21 22:47
 */
@Slf4j
@Component
@Endpoint(id = "logcontent")
public class LogContentEndpoint {

    private final LogDir logDir;

    public LogContentEndpoint(Environment environment) {
        this.logDir = LogDir.get(environment);
        if (this.logDir != null) {
            logDir.applyToSystemProperties();
        }
    }

    @ReadOperation(produces = "text/plain; charset=UTF-8")
    public Resource getLogContent(@Selector String filename) {
        Resource logFileResource = getLogFileResource(filename);
        if (logFileResource == null || !logFileResource.isReadable()) {
            return null;
        }
        return logFileResource;
    }

    private Resource getLogFileResource(String filename) {
        if (this.logDir == null) {
            log.debug("缺少'logging.file.path' 参数");
            return null;
        }
        if (filename == null || filename.isEmpty()) {
            log.debug("缺少'filename' 参数");
            return null;
        }
        String path = this.logDir.toString();
        if (!path.endsWith("/")) {
            path += "/";
        }
        return new FileSystemResource(path + filename);
    }
}
