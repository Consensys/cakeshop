package com.jpmorgan.cakeshop.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

	@SuppressWarnings("rawtypes")
    public static String toString(Object object) {
	    if (object instanceof String) {
	        return (String) object;
	    } else if (object instanceof List) {
	        object = ((List) object).toArray();
	    } else if (object instanceof Map) {
	        return object.toString();
	    }
	    return ToStringBuilder.reflectionToString(object, ShortToStringStyle.INSTANCE);
	}

	public static void puts(Object object) {
	    System.out.println(toString(object));
	}

}
