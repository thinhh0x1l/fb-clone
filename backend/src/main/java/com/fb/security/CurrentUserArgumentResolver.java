package com.fb.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolver tham số @CurrentUser
 * Trích xuất thông tin người dùng hiện tại từ SecurityContext
 * và inject vào controller method thông qua annotation @CurrentUser
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Kiểm tra tham số có được annotate với @CurrentUser
     * và có kiểu WebSocketPrincipal hợp lệ không
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && WebSocketAuthInterceptor.WebSocketPrincipal.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * Giải quyết tham số bằng cách lấy principal từ SecurityContext
     * @return WebSocketPrincipal chứa thông tin người dùng hiện tại hoặc null
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof WebSocketAuthInterceptor.WebSocketPrincipal principal) {
            return principal;
        }
        return null;
    }
}
