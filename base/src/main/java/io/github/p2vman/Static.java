package io.github.p2vman;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Static {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new Identifier.Adapter())
            .setPrettyPrinting()
            .create();
}
