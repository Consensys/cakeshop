package com.jcabi.log;

/**
 * com.jcabi.log.Logger started causing issues in Spring Boot so we simply stub
 * it out for now.
 *
 * @author Chetan Sarva
 */
public class Logger {

    public static void debug(final Object source, final String msg, Object... args) {
    }

    public static void info(final Object source, final String msg, Object... args) {
    }

    public static void warn(final Object source, final String msg, Object... args) {
    }

    public static void warn(final Object source, final String msg) {
    }

    public static void error(final Object source, final String msg, Object... args) {
    }

    public static void format(final String msg, Object... args) {
    }

}
