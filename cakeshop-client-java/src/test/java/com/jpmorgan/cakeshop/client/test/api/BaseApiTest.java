package com.jpmorgan.cakeshop.client.test.api;

import com.jpmorgan.cakeshop.client.ApiClient;

import java.io.IOException;
import java.util.logging.ConsoleHandler;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import feign.Logger;
import feign.Logger.JavaLogger;
import feign.Logger.Level;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;

public class BaseApiTest {

    class StdoutConsoleHandler extends ConsoleHandler {
        public StdoutConsoleHandler() {
            super();
            setOutputStream(System.out);
        }
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(BaseApiTest.class);

    protected MockWebServer mockWebServer;

    protected ApiClient apiClient;

    @BeforeSuite
    public void setupLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
        ConsoleHandler handler = new StdoutConsoleHandler();
        handler.setLevel(java.util.logging.Level.FINE);

        java.util.logging.Logger.getLogger(MockWebServer.class.getName()).addHandler(handler);

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Logger.class.getName());
        logger.setLevel(java.util.logging.Level.FINE);
        logger.addHandler(handler);
    }

    @BeforeClass
    public void setupMockWebserver() throws IOException {
        if (mockWebServer != null) {
            return;
        }
        mockWebServer = new MockWebServer();
        QueueDispatcher dispatcher = new QueueDispatcher();
        dispatcher.setFailFast(true);
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start();
    }

    @AfterClass(alwaysRun=true)
    public void stopMockWebserver() {
        if (mockWebServer != null) {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LOG.debug("MockWebServer shutdown failed", e);
            }
        }
    }

    public String getTestUri() {
        return "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort() + "/cakeshop/api";
    }

    @BeforeMethod
    public void createApiClient() throws IOException {
        this.apiClient = new ApiClient().setBasePath(getTestUri());
        this.apiClient.getFeignBuilder().logger(new JavaLogger()).logLevel(Level.FULL); // set logger to debug
    }

}
