package ru.renthome.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class RequestUtil {
    private RequestUtil() {
    }

    public static Map<String, String> query(HttpExchange exchange) {
        return parse(exchange.getRequestURI().getRawQuery());
    }

    public static Map<String, String> form(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return parse(body);
    }

    public static String value(Map<String, String> data, String key) {
        return data.getOrDefault(key, "").trim();
    }

    public static long longValue(Map<String, String> data, String key) {
        return Long.parseLong(value(data, key));
    }

    public static Integer optionalInt(Map<String, String> data, String key) {
        String value = value(data, key);
        return value.isBlank() ? null : Integer.parseInt(value);
    }

    public static int intValue(Map<String, String> data, String key, int defaultValue) {
        String value = value(data, key);
        return value.isBlank() ? defaultValue : Integer.parseInt(value);
    }

    public static LocalDate dateValue(Map<String, String> data, String key) {
        return LocalDate.parse(value(data, key));
    }

    private static Map<String, String> parse(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            result.put(key, value);
        }
        return result;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
