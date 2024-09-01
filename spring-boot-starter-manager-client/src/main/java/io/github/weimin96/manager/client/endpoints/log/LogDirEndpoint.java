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
import java.util.ArrayList;
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
    public List<LogDirTree> logDir() {
        Resource logDirResource = getLogDirResource();
        try {
            if (logDirResource == null) {
                return null;
            }
            File dir = logDirResource.getFile();
            if (!dir.exists() || !dir.isDirectory() || dir.listFiles() == null) {
                return null;
            }
            List<LogDirTree> result = new ArrayList<>();
            if (dir.exists() && dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    result.add(createTree(file, ""));
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 创建树形结构的方法
    private static LogDirTree createTree(File file, String parentPath) {
        LogDirTree node = new LogDirTree();
        node.setName(file.getName());
        node.setType(file.isDirectory() ? "folder" : "file");
        node.setPath(parentPath.isEmpty() ? file.getName() : parentPath + "/" + file.getName());
        node.setChildren(new ArrayList<>());

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (child.isDirectory() || child.isFile()) {
                    node.getChildren().add(createTree(child ,node.getPath()));
                }
            }
        }

        return node;
    }

    private Resource getLogDirResource() {
        if (this.logDir == null) {
            log.debug("缺少配置 'logging.file.path' properties");
            return null;
        }
        return new FileSystemResource(this.logDir.toString());
    }
}
