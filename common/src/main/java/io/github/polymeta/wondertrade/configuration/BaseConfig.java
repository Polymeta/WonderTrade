package io.github.polymeta.wondertrade.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BaseConfig {
    public static Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public int poolSize = 5;
    public boolean cooldownEnabled = true;
    public int cooldown = 5;
}
