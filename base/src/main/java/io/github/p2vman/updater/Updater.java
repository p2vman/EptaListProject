package io.github.p2vman.updater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class Updater {
    private static Updater updater;
    public static Updater getInstance() {
        if (updater == null) updater = new Updater();
        return updater;
    }
    @Getter
    private String id;

    private static final String API_URL = "https://api.spiget.org/v2/resources/";
    private static final String URL = "https://www.spigotmc.org/";
    private JsonObject object;

    public JsonObject getJson() {
        return object.deepCopy();
    }

    public Updater() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(Updater.class.getClassLoader().getResourceAsStream("updater.json")))) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            this.object = jsonObject;
            id = jsonObject.get("id").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonObject getLasted() throws IOException {
        URL url = new URL(API_URL + id + "/versions/latest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        return JsonParser.parseReader(new BufferedReader(new InputStreamReader(conn.getInputStream()))).getAsJsonObject();
    }

    public String getVersionUrl() throws IOException  {
        URL url = new URL(API_URL + id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        return URL+JsonParser.parseReader(new BufferedReader(new InputStreamReader(conn.getInputStream()))).getAsJsonObject().get("file").getAsJsonObject().get("url").getAsString();
    }
}
