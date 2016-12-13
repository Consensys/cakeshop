package com.jpmorgan.cakeshop.config;

import com.jcabi.manifests.Manifests;

import org.springframework.stereotype.Component;

@Component
public class AppVersion {

    public static final String API_VERSION = "1.0";

    public static final String BUILD_VERSION;
    public static final String BUILD_ID;
    public static final String BUILD_DATE;

    static {
        if (Manifests.exists("Cakeshop-Version")) {
            BUILD_VERSION = Manifests.read("Cakeshop-Version");
            BUILD_ID = Manifests.read("Cakeshop-Build");
            BUILD_DATE = Manifests.read("Cakeshop-Build-Date");
        } else {
            BUILD_VERSION = "";
            BUILD_ID = "";
            BUILD_DATE = "";
        }
    }

}
