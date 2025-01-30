package org.eptalist.storge;

import io.github.p2vman.nbt.NbtIo;
import io.github.p2vman.nbt.tag.Tag;
import io.github.p2vman.nbt.tag.TagCompound;
import io.github.p2vman.nbt.tag.TagList;
import io.github.p2vman.nbt.tag.TagString;
import io.github.p2vman.utils.Pair;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NBT extends ArrayList<String> implements Data<String> {
    private final NbtIo io = new NbtIo();
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

    public NBT(Map<String, Object> data) {
        this.data = data;
        if (!(size()>0)) {
            load();
        }
    }

    public void load() {
        clear();

        try {
            InputStream stream = new FileInputStream((String) this.data.get("file"));

            if (this.data.containsKey("gzip") && (Boolean) this.data.get("gzip")) {
                stream = new GZIPInputStream(stream);
            }
            DataInputStream datastream = new DataInputStream(stream);

            Pair<String, Tag> tagr = io.read(datastream);
            if (tagr.v instanceof TagCompound) {
                TagCompound compound = (TagCompound) tagr.v;
                if (compound.is("whitelist") && (compound.get("whitelist") instanceof TagList)) {
                    TagList list = (TagList) compound.get("whitelist");
                    Iterator<Tag> it =  list.iterator();
                    while (it.hasNext()) {
                        Tag tag = it.next();
                        if (tag instanceof TagString) {
                            add(((TagString) tag).getValue());
                        }
                    }
                }
            }
            else {
                save();
            }
            datastream.close();
        } catch (Exception e) {
            save();
        }
    }

    public void save() {
        try {
            OutputStream stream = new FileOutputStream((String) this.data.get("file"));

            if (this.data.containsKey("gzip") && (Boolean) this.data.get("gzip")) {
                stream = new GZIPOutputStream(stream);
            }
            DataOutputStream datastream = new DataOutputStream(stream);

            TagCompound compound = new TagCompound();
            {
                TagList list = new TagList(Tag.TagString);
                for (String s : this) list.add(new TagString(s));
                compound.put("whitelist", list);
            }
            io.write(datastream, compound);
            datastream.close();
        } catch (Exception e) {
            save();
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
    public boolean is(String name, List<String> info) {
        return contains(name);
    }

    @Override
    public boolean removeUser(String name, List<String> info) {
        if (!is(name)) {
            info.add("&r" + name + "is not in the whitelist");
            return false;
        }
        return remove(name);
    }

    @Override
    public boolean addUser(String name, List<String> info) {
        if (is(name)) {
            info.add("&r" + name + "is already on the whitelist");
            return false;
        }
        return add(name);
    }
}
