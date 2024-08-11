package io.github.weimin96.manager.server.web.reactive;

import io.github.weimin96.manager.server.utils.Util;
import io.github.weimin96.manager.server.web.AdminController;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.result.condition.PatternsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * @author panwm
 * @since 2024/8/4 0:16
 */
public class AdminControllerHandlerMapping extends RequestMappingHandlerMapping {

    private final String contextPath;

    public AdminControllerHandlerMapping(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return AnnotatedElementUtils.hasAnnotation(beanType, AdminController.class);
    }

    @Override
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        super.registerHandlerMethod(handler, method, withPrefix(mapping));
    }

    private RequestMappingInfo withPrefix(RequestMappingInfo mapping) {
        if (!StringUtils.hasText(contextPath)) {
            return mapping;
        }
        PatternsRequestCondition patternsCondition = new PatternsRequestCondition(
                withNewPatterns(mapping.getPatternsCondition().getPatterns()));
        return new RequestMappingInfo(patternsCondition, mapping.getMethodsCondition(), mapping.getParamsCondition(),
                mapping.getHeadersCondition(), mapping.getConsumesCondition(), mapping.getProducesCondition(),
                mapping.getCustomCondition());
    }

    private List<PathPattern> withNewPatterns(Set<PathPattern> patterns) {
        return patterns.stream()
                .map((pattern) -> getPathPatternParser().parse(Util.normalizePath(contextPath + pattern)))
                .collect(toList());
    }
}
