package com.example.demo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        System.out.println("=== REQUEST LOGGING ===");
        System.out.println("Method: " + req.getMethod());
        System.out.println("URI: " + req.getRequestURI());
        System.out.println("Auth header: " + req.getHeader("Authorization"));
        System.out.println("Origin: " + req.getHeader("Origin"));
        System.out.println("========================");

        chain.doFilter(request, response);
    }
}