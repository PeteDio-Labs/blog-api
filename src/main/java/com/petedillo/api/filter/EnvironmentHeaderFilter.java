package com.petedillo.api.filter;

import com.petedillo.api.config.AppConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EnvironmentHeaderFilter implements Filter {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String environment = appConfig.getEnvironment();
        String version = appConfig.getVersion();

        if (environment != null) {
            httpResponse.setHeader("X-Environment", environment);
        }
        if (version != null) {
            httpResponse.setHeader("X-API-Version", version);
        }

        chain.doFilter(request, response);
    }
}
