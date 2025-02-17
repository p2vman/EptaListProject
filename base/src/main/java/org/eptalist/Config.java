package org.eptalist;

import com.google.gson.annotations.SerializedName;
import io.github.p2vman.Identifier;
import io.github.p2vman.Static;
import io.github.p2vman.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    public boolean enable = false;
    public boolean skip_to_op = true;
    public Identifier curent = new Identifier("whitelist", "base");
    public Mode[] modes = new Mode[] {
            new Mode(
                    new Identifier(
                            "whitelist",
                            "base"),
                    "org.eptalist.storge.Json",
                    Utils.put(new HashMap<>(), "file", "eptalist.json"),
                    "&6Молодой человек а вас нету в списке &4:)"
            ),
            new Mode(
                    new Identifier(
                            "whitelist",
                            "dev"),
                    "org.eptalist.storge.Json",
                    Utils.put(new HashMap<>(), "file", "eptalist.dev.json"),
                    "&2Сервер на тех работах"
            )
    };

    public static class Mode {
        public Identifier id;
        public String storage;

        public Map<String, Object> data;
        @SerializedName("msg")
        public String kick_msg;
        public Mode(Identifier id, String storage, Map<String, Object> data, String kick_msg) {
            this.id = id;
            this.storage = storage;
            this.data = data;
            this.kick_msg = kick_msg;
        }
    }

    public static class ConfigContainer {
        private File config;
        private Config cfg = null;
        public ConfigContainer(File config) {
            this.config = config;
        }

        public Config get() {
            if (cfg == null) {
                load();
            }
            return cfg;
        }

        public void load() {
            try {
                if (!config.exists()) {
                    this.cfg = new Config();
                    try (FileWriter writer =new FileWriter(this.config)) {
                        Static.GSON.toJson(cfg, writer);
                    }
                }
                else {
                    try (FileReader reader = new FileReader(this.config)) {
                        cfg = Static.GSON.fromJson(reader, Config.class);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void save() {
            try (FileWriter writer = new FileWriter(this.config)) {
                Static.GSON.toJson(cfg, writer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
