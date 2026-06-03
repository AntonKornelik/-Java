package ru.renthome.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;

public final class Html {
    private Html() {
    }

    public static void page(HttpExchange exchange, User user, String title, String content) throws IOException {
        String body = """
                <!doctype html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s - RentHome</title>
                    <link rel="stylesheet" href="/assets/style.css">
                </head>
                <body>
                    <header class="topbar">
                        <a class="brand" href="/">RentHome</a>
                        <nav>%s</nav>
                    </header>
                    <main>%s</main>
                </body>
                </html>
                """.formatted(escape(title), nav(user), content);
        send(exchange, 200, body);
    }

    public static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    public static void error(HttpExchange exchange, User user, String message) throws IOException {
        page(exchange, user, "Ошибка", """
                <section class="narrow">
                    <div class="notice danger">%s</div>
                    <a class="button ghost" href="/">На главную</a>
                </section>
                """.formatted(escape(message)));
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String option(String value, String label, String selected) {
        return "<option value=\"" + escape(value) + "\"" + (value.equals(selected) ? " selected" : "") + ">"
                + escape(label) + "</option>";
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static String nav(User user) {
        if (user == null) {
            return """
                    <a href="/listings">Объявления</a>
                    <a href="/auth/login">Вход</a>
                    <a class="button small" href="/auth/register">Регистрация</a>
                    """;
        }
        String ownerLink = user.getRole() == Role.OWNER ? "<a href=\"/listings/new\">Добавить жильё</a>" : "";
        String favoriteLink = user.getRole() == Role.TENANT ? "<a href=\"/favorites\">Избранное</a>" : "";
        String adminLink = user.getRole() == Role.ADMIN ? "<a href=\"/admin\">Админка</a>" : "";
        return """
                <a href="/listings">Объявления</a>
                %s
                <a href="/bookings">Заявки</a>
                %s
                %s
                <a href="/profile">%s</a>
                <form class="inline" action="/auth/logout" method="post"><button>Выход</button></form>
                """.formatted(ownerLink, favoriteLink, adminLink, escape(user.getName()));
    }
}
