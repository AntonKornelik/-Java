package ru.renthome.util;

import com.sun.net.httpserver.HttpExchange;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import ru.renthome.entity.User;
import ru.renthome.repository.UserRepository;

public class SessionManager {
    private final UserRepository users;
    private static final String COOKIE_NAME = "RH_USER";

    public SessionManager(UserRepository users) {
        this.users = users;
    }

    public Optional<User> currentUser(HttpExchange exchange) {
        return cookieValue(exchange, COOKIE_NAME).flatMap(this::restore).map(user -> {
            refreshCookie(exchange, user);
            return user;
        });
    }

    public void login(HttpExchange exchange, User user) {
        refreshCookie(exchange, user);
        clearLegacyCookies(exchange);
    }

    public void logout(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Set-Cookie", COOKIE_NAME + "=; Path=/; Max-Age=0; HttpOnly");
        clearLegacyCookies(exchange);
    }

    private Optional<String> cookieValue(HttpExchange exchange, String cookieName) {
        List<String> cookies = exchange.getRequestHeaders().getOrDefault("Cookie", List.of());
        for (String header : cookies) {
            for (String rawCookie : header.split(";")) {
                String[] parts = rawCookie.trim().split("=", 2);
                if (parts.length == 2 && cookieName.equals(parts[0].trim()) && !parts[1].isBlank()) {
                    return Optional.of(parts[1].trim());
                }
            }
        }
        return Optional.empty();
    }

    private void refreshCookie(HttpExchange exchange, User user) {
        String value = user.getId() + "-" + signature(user);
        exchange.getResponseHeaders().add("Set-Cookie",
                COOKIE_NAME + "=" + value + "; Path=/; Max-Age=86400; HttpOnly; SameSite=Lax");
    }

    private Optional<User> restore(String cookie) {
        String[] parts = cookie.split("-", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }
        try {
            long userId = Long.parseLong(parts[0]);
            return users.findById(userId)
                    .filter(user -> !user.isBlocked())
                    .filter(user -> signature(user).equals(parts[1]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private void clearLegacyCookies(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Set-Cookie", "RH_SESSION=; Path=/; Max-Age=0; HttpOnly");
        for (int port = 8080; port <= 8095; port++) {
            exchange.getResponseHeaders().add("Set-Cookie", "RH_SESSION_" + port + "=; Path=/; Max-Age=0; HttpOnly");
        }
    }

    private String signature(User user) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update((user.getId() + ":" + user.getEmail() + ":" + user.getPasswordHash())
                    .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest()).substring(0, 24);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
