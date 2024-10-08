package io.github.weimin96.manager.server.domain.value;

import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * actuator endpoint url
 * @author panwm
 * @since 2024/8/3 22:31
 */
@lombok.Data
public class Endpoint implements Serializable {

    public static final String INFO = "info";

    public static final String HEALTH = "health";

    public static final String LOGFILE = "logfile";

    public static final String LOG_DIR = "logdir";

    public static final String LOG_CONTENT= "logcontent";

    public static final String ENV = "env";

    public static final String HTTPTRACE = "httptrace";

    public static final String THREADDUMP = "threaddump";

    public static final String ACTUATOR_INDEX = "actuator-index";

    public static final String BEANS = "beans";

    public static final String CONFIGPROPS = "configprops";

    public static final String MAPPINGS = "mappings";

    public static final String STARTUP = "startup";

    private final String id;

    private final String url;

    private Endpoint(String id, String url) {
        Assert.hasText(id, "'id' must not be empty.");
        Assert.hasText(url, "'url' must not be empty.");
        this.id = id;
        this.url = url;
    }

    public static Endpoint of(String id, String url) {
        if ("logcontent-filePath".equals(id)) {
            return new Endpoint("logcontent", url.replace("/{filePath}", ""));
        }
        return new Endpoint(id, url);
    }

}
