package com.project.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String path = request.getRequestURI();

        // Check if the user is trying to access protected contractor or employee routes
        if (path.startsWith("/contractor/")) {
            if (session == null || session.getAttribute("loggedInContractor") == null) {
                response.sendRedirect("/login?error=unauthorized");
                return false;
            }
        } else if (path.startsWith("/employee/")) {
            if (session == null || session.getAttribute("loggedInEmployee") == null) {
                response.sendRedirect("/login?error=unauthorized");
                return false;
            }
        }

        return true;
    }
}
