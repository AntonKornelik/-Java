package ru.renthome.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import ru.renthome.entity.User;
import ru.renthome.util.Html;
import ru.renthome.util.SessionManager;

public abstract class BaseController implements HttpHandler {
    protected final SessionManager sessions;

    protected BaseController(SessionManager sessions) {
        this.sessions = sessions;
    }

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        User user = sessions.currentUser(exchange).orElse(null);
        try {
            handle(exchange, user);
        } catch (IllegalArgumentException e) {
            Html.error(exchange, user, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Html.error(exchange, user, "Внутренняя ошибка приложения: " + e.getMessage());
        }
    }

    protected abstract void handle(HttpExchange exchange, User user) throws Exception;

    protected void requireLogin(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Сначала войдите в аккаунт");
        }
    }
}
