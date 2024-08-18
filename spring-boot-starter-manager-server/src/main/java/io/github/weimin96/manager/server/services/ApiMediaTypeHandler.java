package io.github.weimin96.manager.server.services;

import org.springframework.boot.actuate.endpoint.ApiVersion;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

/**
 *
 * @author panwm
 * @since 2024/8/4 23:57
 */
public class ApiMediaTypeHandler {

    public boolean isApiMediaType(MediaType mediaType) {
        return Stream.of(ApiVersion.values()).map(ApiVersion::getProducedMimeType)
                .anyMatch((mimeType) -> mimeType.isCompatibleWith(mediaType));
    }
}
