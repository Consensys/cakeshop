package com.jpmorgan.cakeshop.test;

import static org.testng.Assert.*;

public class Assert {

    static public void assertNotEmptyString(String str) {
        assertTrue(str != null && str.length() > 0, null);
    }

    static public void assertNotEmptyString(String str, String message) {
        assertTrue(str != null && str.length() > 0, message);
    }

}
