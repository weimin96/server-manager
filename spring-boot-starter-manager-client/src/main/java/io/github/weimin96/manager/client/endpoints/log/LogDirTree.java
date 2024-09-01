package io.github.weimin96.manager.client.endpoints.log;

import lombok.Data;

import java.util.List;

/**
 * @author panwm
 * @since 2024/9/1 23:08
 */
@Data
public class LogDirTree {

    private String name;

    private String type;

    private String path;

    private List<LogDirTree> children;
}
