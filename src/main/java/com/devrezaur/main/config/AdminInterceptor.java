package com.devrezaur.main.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String servletPath = request.getServletPath();

        if (servletPath.startsWith("/admin") && !servletPath.equals("/admin/login") && !servletPath.equals("/admin/logout")) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("isAdmin") == null || !(Boolean) session.getAttribute("isAdmin")) {
                response.sendRedirect(request.getContextPath() + "/admin/login");
                return false;
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            org.springframework.web.servlet.ModelAndView modelAndView) {
        if (request.getServletPath().startsWith("/admin")) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }
    }
}
