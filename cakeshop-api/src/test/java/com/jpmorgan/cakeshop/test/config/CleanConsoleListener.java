package com.jpmorgan.cakeshop.test.config;

import org.apache.commons.lang3.StringUtils;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class CleanConsoleListener extends TestListenerAdapter {

    private String testClass;

    public CleanConsoleListener() {
    }

    @Override
    public void beforeConfiguration(ITestResult tr) {
        super.beforeConfiguration(tr);

        if (testClass == null || !testClass.equalsIgnoreCase(tr.getTestClass().getName())) {
            testClass = tr.getTestClass().getName();
            String log = "# " + testClass.toString() + " #";
            System.out.println(StringUtils.repeat("#", log.length()));
            System.out.println(log);
            System.out.println(StringUtils.repeat("#", log.length()));
        }

        String type = null;
        if (tr.getMethod().isBeforeClassConfiguration()) {
            type = "before class";
        } else if (tr.getMethod().isBeforeMethodConfiguration()) {
            type = "before method";
        } else if (tr.getMethod().isBeforeSuiteConfiguration()) {
            type = "before suite";
        } else if (tr.getMethod().isBeforeTestConfiguration()) {
            type = "before test";

        } else if (tr.getMethod().isAfterClassConfiguration()) {
            type = "after class";
        } else if (tr.getMethod().isAfterMethodConfiguration()) {
            type = "after method";
        } else if (tr.getMethod().isAfterSuiteConfiguration()) {
            type = "after suite";
        } else if (tr.getMethod().isAfterTestConfiguration()) {
            type = "after test";
        }

        // uncomment for extra debugging
        System.out.println();
        System.out.println("[" + type + "] " + tr.getName());
        System.out.println();
    }

    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);

        String log = "# START:  " + result.getName() + " #";

        System.out.println();
        System.out.println(StringUtils.repeat("#", log.length()));

        System.out.println(log);

        System.out.println(StringUtils.repeat("#", log.length()));
        System.out.println();
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        super.onTestFailure(tr);
        testEnd(tr, "FAIL");
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        super.onTestSkipped(tr);
        testEnd(tr, "SKIP");
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);
        testEnd(tr, "PASS");
    }

    private void testEnd(ITestResult result, String status) {
        System.out.println();
        System.out.println("### END " + result.getName() + " (" + status + ") " + " ###");
        System.out.println();
    }


}
