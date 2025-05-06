package io.github.p2vman.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;
import java.util.Scanner;

public class MiniConfig {

    public static JsonObject parse(String input) {
        return parseBlock(new Scanner(input));
    }

    private static JsonObject parseBlock(Scanner scanner) {
        JsonObject obj = new JsonObject();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

            if (line.endsWith("{")) {
                String key = line.substring(0, line.length() - 1).trim();
                JsonObject nested = parseBlock(scanner);
                obj.add(key, nested);
            } else if (line.equals("}")) {
                break;
            } else if (line.endsWith("[")) {
                String key = line.substring(0, line.length() - 1).trim();
                JsonArray array = parseArrayBlock(scanner);
                obj.add(key, array);
            } else {
                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                String value = parts[1].trim();
                obj.add(key, parseValue(value));
            }
        }
        return obj;
    }

    private static JsonArray parseArrayBlock(Scanner scanner) {
        JsonArray array = new JsonArray();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.equals("]")) break;
            if (line.equals("{")) {
                array.add(parseBlock(scanner));
            } else {
                array.add(parseValue(line));
            }
        }
        return array;
    }

    private static JsonElement parseValue(String value) {
        if (value.equals("true") || value.equals("false")) return new JsonPrimitive(Boolean.parseBoolean(value));
        try { return new JsonPrimitive(Integer.parseInt(value)); } catch (NumberFormatException ignored) {}
        try { return new JsonPrimitive(Double.parseDouble(value)); } catch (NumberFormatException ignored) {}
        if (value.startsWith("\"") && value.endsWith("\"")) return new JsonPrimitive(value.substring(1, value.length() - 1));
        return new JsonPrimitive(value);
    }

    public static String toMiniConf(JsonObject obj, int indent) {
        StringBuilder sb = new StringBuilder();
        writeObject(sb, obj, indent);
        return sb.toString();
    }

    private static void writeObject(StringBuilder sb, JsonObject obj, int indent) {
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String pad = indent(indent);
            String key = entry.getKey();
            JsonElement val = entry.getValue();

            if (val.isJsonObject()) {
                sb.append(pad).append(key).append(" {\n");
                writeObject(sb, val.getAsJsonObject(), indent + 2);
                sb.append(pad).append("}\n");
            } else if (val.isJsonArray()) {
                sb.append(pad).append(key).append(" [\n");
                writeArray(sb, val.getAsJsonArray(), indent + 2);
                sb.append(pad).append("]\n");
            } else {
                sb.append(pad).append(key).append(" = ").append(formatValue(val)).append("\n");
            }
        }
    }

    private static void writeArray(StringBuilder sb, JsonArray array, int indent) {
        for (JsonElement el : array) {
            String pad = indent(indent);
            if (el.isJsonObject()) {
                sb.append(pad).append("{\n");
                writeObject(sb, el.getAsJsonObject(), indent + 2);
                sb.append(pad).append("}\n");
            } else {
                sb.append(pad).append(formatValue(el)).append("\n");
            }
        }
    }

    private static String formatValue(JsonElement val) {
        if (val.isJsonPrimitive()) {
            JsonPrimitive prim = val.getAsJsonPrimitive();
            if (prim.isString()) return "\"" + prim.getAsString() + "\"";
            return prim.toString();
        }
        return val.toString();
    }

    private static String indent(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(' ');
        return sb.toString();
    }
}