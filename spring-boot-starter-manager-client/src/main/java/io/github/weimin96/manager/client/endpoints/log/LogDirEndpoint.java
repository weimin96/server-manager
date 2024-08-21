package io.github.weimin96.manager.client.endpoints.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * describe:
 *
 * @author panwm
 * @since 2024/8/20 18:07
 */
@Component
@Endpoint(id = "logdir")
@Slf4j
public class LogDirEndpoint {

    private final LogDir logDir;

    public LogDirEndpoint(Environment environment) {
        this.logDir = LogDir.get(environment);
        if (this.logDir != null) {
            logDir.applyToSystemProperties();
        }
    }

    @ReadOperation
    public List<String> logDir() {
        Resource logDirResource = getLogDirResource();
        try {
            if (logDirResource == null) {
                return null;
            }
            File dir = logDirResource.getFile();
            if (!dir.exists() || !dir.isDirectory() || dir.listFiles() == null) {
                return null;
            }
            return Stream.of(dir.listFiles()).sorted(Comparator.comparingLong(File::lastModified).reversed()).map(File::getName).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Resource getLogDirResource() {
        if (this.logDir == null) {
            log.debug("缺少配置 'logging.file.path' properties");
            return null;
        }
        return new FileSystemResource(this.logDir.toString());
    }
}
