package com.jpmorgan.cakeshop.config;

import com.jcabi.manifests.Manifests;
import com.jcabi.manifests.ServletMfs;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages = "com.jpmorgan.cakeshop")
public class WebAppInit extends SpringBootServletInitializer {

    public static void setLoggingPath(boolean isSpringBoot) {
        // setup logging path for spring-boot
        if (StringUtils.isNotBlank(System.getProperty("logging.path"))) {
            return;
        }
        if (isSpringBoot) {
            System.setProperty("logging.path", "logs");
            return;
        }

        // running in a container, find home path
        if (StringUtils.isNotBlank(System.getProperty("catalina.base"))
                && StringUtils.isBlank(System.getProperty("logging.path"))) {
            // tomcat
            System.setProperty("logging.path", System.getProperty("catalina.base") + "/logs");

        } else if (StringUtils.isNotBlank(System.getProperty("catalina.home"))
                && StringUtils.isBlank(System.getProperty("logging.path"))) {
            // tomcat
            System.setProperty("logging.path", System.getProperty("catalina.home") + "/logs");

        } else if (StringUtils.isNotBlank(System.getProperty("jetty.logging.dir"))
                && StringUtils.isBlank(System.getProperty("logging.path"))) {
            // jetty
            System.setProperty("logging.path", System.getProperty("jetty.logging.dir"));

        } else if (StringUtils.isNotBlank(System.getProperty("jetty.home"))
                && StringUtils.isBlank(System.getProperty("logging.path"))) {
            // jetty
            System.setProperty("logging.path", System.getProperty("jetty.home") + "/logs");

        } else {
            // use /tmp
            System.err.println("WARNING: could not detect appserver home; writing logs to /tmp");
            System.err.println();
            System.err.println("Dumping system props:");
            System.err.println(System.getProperties());
            System.err.println();
            System.setProperty("logging.path", "/tmp");
        }

    }

//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//        return builder.profiles(env.getActiveProfiles());
//    }
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        setLoggingPath(false);
        try {
            Manifests.DEFAULT.append(new ServletMfs(container));
        } catch (IOException e) {
            System.err.println("Failed to load servlet manifest: " + e.getMessage());
        }
        container.addListener(new SessionListener());
        super.onStartup(container);

    }

}
