package io.github.weimin96.manager.client.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
@Endpoint(id = "loghistory")
@Slf4j
public class LogHistoryEndpoint {

    private final LogDir logDir;

    public LogHistoryEndpoint(Environment environment) {
        this.logDir = LogDir.get(environment);
        if (this.logDir != null) {
            logDir.applyToSystemProperties();
        }
    }

    @ReadOperation(produces = "text/plain; charset=UTF-8")
    public List<Resource> logHistory() {
        Resource logDirResource = getLogDirResource();
        if (logDirResource == null || !logDirResource.isReadable()) {
            return null;
        }
        try {
            File dir = logDirResource.getFile();
            if (dir.exists() && dir.isDirectory() && dir.listFiles() == null) {
                return Stream.of(dir.listFiles()).map(e -> new FileSystemResource(e.getAbsolutePath())).collect(Collectors.toList());
            } else {
                log.error("日志目录不存在或不是目录: " + logDir.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ArrayList<>();
    }

    private Resource getLogDirResource() {
        if (this.logDir == null) {
            log.debug("缺少配置 'logging.file.path' properties");
            return null;
        }
        return new FileSystemResource(this.logDir.toString());
    }
}
