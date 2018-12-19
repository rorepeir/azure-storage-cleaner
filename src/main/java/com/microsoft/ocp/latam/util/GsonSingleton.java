package com.microsoft.ocp.latam.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonSingleton {

    private static Gson ourInstance = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .create();

    public static Gson getInstance() {
        return ourInstance;
    }

    private GsonSingleton() {
    }

}