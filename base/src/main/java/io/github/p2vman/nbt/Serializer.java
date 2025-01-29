package io.github.p2vman.nbt;


import io.github.p2vman.nbt.tag.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
public class Serializer {
    private NbtIo io;
    public Tag get(Field field, Object obj) throws IllegalArgumentException, IllegalAccessException {
        Object fobj = field.get(obj);
        if (fobj instanceof Integer) {
            return new TagInt((Integer) fobj);
        } else if (fobj instanceof Byte) {
            return new TagByte((Byte) fobj);
        } else if (fobj instanceof Short) {
            return new TagShort((Short) fobj);
        } else if (fobj instanceof String) {
            return new TagString((String) fobj);
        } else if (fobj instanceof Long) {
            return new TagLong((Long) fobj);
        } else {
            return new TagEnd();
        }
    }

    public TagCompound objr(Object obj) throws IllegalArgumentException, IllegalAccessException {
        TagCompound compound = new TagCompound();
        Class<?> cls = obj.getClass();
        compound.put("!", new TagString(cls.getTypeName()));

        for (Field field : cls.getFields()) {
            Tag tag = get(field, obj);
            if (tag instanceof TagEnd) {
                compound.put(field.getName(), objr(field.get(obj)));
            } else {
                compound.put(field.getName(), tag);
            }
        }
        return compound;
    }

    public Serializer(NbtIo io) {
        this.io = io;
    }

    public void write(DataOutputStream stream, Object obj) throws IOException, IllegalArgumentException, IllegalAccessException {
        io.write(stream, objr(obj));
    }
}
