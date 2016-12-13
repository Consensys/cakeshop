package com.jpmorgan.cakeshop.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class SortedProperties extends Properties {

    private static final long serialVersionUID = -7002858041811939535L;

    public static void store(Properties props, OutputStream out) throws IOException {
        SortedProperties sprops = new SortedProperties();
        sprops.putAll(props);
        sprops.store(out, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Enumeration keys() {
        Enumeration keysEnum = super.keys();
        Vector<String> keyList = new Vector<>();
        while (keysEnum.hasMoreElements()) {
            keyList.add((String) keysEnum.nextElement());
        }
        Collections.sort(keyList);
        return keyList.elements();
    }

}
