package io.github.p2vman.nbt.tag;

import io.github.p2vman.nbt.TagReader;
import io.github.p2vman.nbt.TagWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Tag {
    public static final char TagByte = 1;
    public static final char TagShort = 2;
    public static final char TagInt = 3;
    public static final char TagLong = 4;
    public static final char TagFloat = 5;
    public static final char TagDouble = 6;
    public static final char TagByteArray = 7;
    public static final char TagString = 8;
    public static final char TagList = 9;
    public static final char TagCompound = 10;
    public static final char TagIntArray = 11;
    public static final char TagLongArray = 12;
    public abstract char getID();


    public abstract void write(DataOutputStream stream, TagWriter writer) throws IOException;
    public abstract void read(DataInputStream stream, TagReader reader) throws IOException;
}
