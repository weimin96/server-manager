package io.github.weimin96.manager.client.endpoints.log;

import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Properties;

/**
 * describe:
 *
 * @author panwm
 * @since 2024/8/20 18:25
 */
public class LogDir {

    public static final String FILE_PATH_PROPERTY = "logging.file.path";

    private final String path;

    public LogDir(String path) {
        Assert.isTrue(StringUtils.hasLength(path), "logging.file.path 不能为空");
        this.path = path;
    }

    public void applyToSystemProperties() {
        this.applyTo(System.getProperties());
    }

    public void applyTo(Properties properties) {
        this.put(properties, "LOG_PATH", this.path);
    }

    private void put(Properties properties, String key, String value) {
        if (StringUtils.hasLength(value)) {
            properties.put(key, value);
        }
    }

    @Override
    public String toString() {
        return (new File(this.path)).getPath();
    }

    public static LogDir get(PropertyResolver propertyResolver) {
        String path = propertyResolver.getProperty("logging.file.path");
        return !StringUtils.hasLength(path) ? null : new LogDir(path);
    }
}
