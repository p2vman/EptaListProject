package io.github.p2vman.eptalist.storge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.p2vman.Static;
import io.github.p2vman.lang.Lang;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class Json extends ArrayList<String> implements Data<String> {
    @Override
    public void close() throws IOException {
        save();
    }

    @Override
    public boolean addUser(String name) {
        if (is(name)) {
            return false;
        }
        return add(name);
    }

    @Override
    public boolean is(String name) {
        return contains(name);
    }

    @Override
    public boolean removeUser(String name) {
        if (!is(name)) {

            return false;
        }
        return remove(name);
    }

    public Map<String, Object> data;

    public Json(Map<String, Object> data) {
        this.data = data;
        if (!(size()>0)) {
            load();
        }
    }

    public void load() {
        clear();
        try {
            JsonArray array = Static.GSON.fromJson(new InputStreamReader(new FileInputStream((String) this.data.get("file"))), JsonArray.class);
            Iterator<JsonElement> iterator = array.iterator();
            while (iterator.hasNext()) {
                add(iterator.next().getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            save();
        }
    }

    public void save() {
        try {
            OutputStream stream = new FileOutputStream((String) this.data.get("file"));
            stream.write(Static.GSON.toJson(this).getBytes(StandardCharsets.UTF_8));
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean add(String s) {
        if (super.add(s)) {
            save();
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean remove(Object o) {
        if (super.remove(o)) {
            save();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void sort(Comparator<? super String> c) {
        super.sort(c);
        save();
    }

    @Override
    public List<String> toList() {
        return this;
    }

    @Override
    public boolean is(String name, Consumer<String> info) {
        return contains(name);
    }

    @Override
    public boolean removeUser(String name, Consumer<String> info) {
        if (!is(name)) {
            info.accept(Lang.LANG.format("storge.remove.not.in", name));
            return false;
        }
        return remove(name);
    }

    @Override
    public boolean addUser(String name, Consumer<String> info) {
        if (is(name)) {
            info.accept(Lang.LANG.format("storge.add.is.already", name));
            return false;
        }
        return add(name);
    }
}
