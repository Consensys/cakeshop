package com.jpmorgan.cakeshop.config;

import com.jcabi.manifests.Manifests;
import com.jcabi.manifests.ServletMfs;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages = "com.jpmorgan.cakeshop")
public class WebAppInit extends SpringBootServletInitializer {

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        try {
            Manifests.DEFAULT.append(new ServletMfs(container));
        } catch (IOException e) {
            System.err.println("Failed to load servlet manifest: " + e.getMessage());
        }
        container.addListener(new SessionListener());
        super.onStartup(container);
    }
}
