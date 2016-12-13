package com.jpmorgan.cakeshop.client.proxy.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import com.jpmorgan.cakeshop.client.proxy.ValueConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
public @interface Transact {
    String value() default ValueConstants.DEFAULT_NONE;
}
