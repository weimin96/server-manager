package io.github.weimin96.manager.server.web.servlet;

import io.github.weimin96.manager.server.utils.Util;
import io.github.weimin96.manager.server.web.AdminController;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 自定义处理器映射类 动态添加contextPath
 * @author panwm
 * @since 2024/8/3 20:45
 */
public class AdminControllerHandlerMapping extends RequestMappingHandlerMapping {

    private String contextPath;

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

    /**
     * 添加前缀
     * @param mapping mapping
     * @return RequestMappingInfo
     */
    private RequestMappingInfo withPrefix(RequestMappingInfo mapping) {
        if (!StringUtils.hasText(this.contextPath)) {
            return mapping;
        }
        PatternsRequestCondition patternsCondition = new PatternsRequestCondition(
                withNewPatterns(mapping.getPatternsCondition().getPatterns()));

        return new RequestMappingInfo(patternsCondition, mapping.getMethodsCondition(), mapping.getParamsCondition(),
                mapping.getHeadersCondition(), mapping.getConsumesCondition(), mapping.getProducesCondition(),
                mapping.getCustomCondition());
    }

    private String[] withNewPatterns(Set<String> patterns) {
        return patterns.stream().map((pattern) -> Util.normalizePath(this.contextPath + pattern))
                .toArray(String[]::new);
    }
}
