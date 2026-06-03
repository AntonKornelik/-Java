package ru.renthome.controller;

import com.sun.net.httpserver.HttpExchange;
import java.util.Map;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.service.UserService;
import ru.renthome.util.Html;
import ru.renthome.util.RequestUtil;
import ru.renthome.util.SessionManager;

public class AuthController extends BaseController {
    private final UserService users;

    public AuthController(UserService users, SessionManager sessions) {
        super(sessions);
        this.users = users;
    }

    @Override
    protected void handle(HttpExchange exchange, User user) throws Exception {
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/login") && "POST".equals(exchange.getRequestMethod())) {
            login(exchange);
        } else if (path.endsWith("/login")) {
            loginForm(exchange, user);
        } else if (path.endsWith("/register") && "POST".equals(exchange.getRequestMethod())) {
            register(exchange);
        } else if (path.endsWith("/register")) {
            registerForm(exchange, user);
        } else if (path.endsWith("/logout") && "POST".equals(exchange.getRequestMethod())) {
            sessions.logout(exchange);
            Html.redirect(exchange, "/");
        } else {
            Html.redirect(exchange, "/auth/login");
        }
    }

    private void loginForm(HttpExchange exchange, User user) throws Exception {
        if (user != null) {
            successPage(exchange, user, "Вы уже вошли в аккаунт");
            return;
        }
        Html.page(exchange, user, "Вход", """
                <section class="narrow">
                    <h1>Вход</h1>
                    <form class="form" action="/auth/login" method="post">
                        <label>Email<input type="email" name="email" required></label>
                        <label>Пароль<input type="password" name="password" required></label>
                        <button class="button">Войти</button>
                    </form>
                    <div class="demo-logins">
                        <form action="/auth/login" method="post">
                            <input type="hidden" name="email" value="tenant@renthome.local">
                            <input type="hidden" name="password" value="tenant">
                            <button class="button ghost">Войти как арендатор</button>
                        </form>
                        <form action="/auth/login" method="post">
                            <input type="hidden" name="email" value="owner@renthome.local">
                            <input type="hidden" name="password" value="owner">
                            <button class="button ghost">Войти как владелец</button>
                        </form>
                        <form action="/auth/login" method="post">
                            <input type="hidden" name="email" value="admin@renthome.local">
                            <input type="hidden" name="password" value="admin">
                            <button class="button ghost">Войти как админ</button>
                        </form>
                    </div>
                </section>
                """);
    }

    private void registerForm(HttpExchange exchange, User user) throws Exception {
        if (user != null) {
            successPage(exchange, user, "Вы уже зарегистрированы и вошли в аккаунт");
            return;
        }
        Html.page(exchange, user, "Регистрация", """
                <section class="narrow">
                    <h1>Регистрация</h1>
                    <form class="form" action="/auth/register" method="post">
                        <label>Имя<input name="name" required></label>
                        <label>Email<input type="email" name="email" required></label>
                        <label>Пароль<input type="password" name="password" required></label>
                        <label>Роль<select name="role">%s%s</select></label>
                        <button class="button">Создать аккаунт</button>
                    </form>
                </section>
                """.formatted(Html.option("TENANT", "Арендатор", "TENANT"),
                Html.option("OWNER", "Владелец", "")));
    }

    private void login(HttpExchange exchange) throws Exception {
        Map<String, String> form = RequestUtil.form(exchange);
        User user = users.login(RequestUtil.value(form, "email"), RequestUtil.value(form, "password"));
        sessions.login(exchange, user);
        successPage(exchange, user, "Вход выполнен");
    }

    private void register(HttpExchange exchange) throws Exception {
        Map<String, String> form = RequestUtil.form(exchange);
        User user = users.register(RequestUtil.value(form, "name"), RequestUtil.value(form, "email"),
                RequestUtil.value(form, "password"), Role.valueOf(RequestUtil.value(form, "role")));
        sessions.login(exchange, user);
        successPage(exchange, user, "Аккаунт создан");
    }

    private void successPage(HttpExchange exchange, User user, String title) throws Exception {
        String roleAction = switch (user.getRole()) {
            case OWNER -> "<a class=\"button\" href=\"/listings/new\">Добавить первое объявление</a>";
            case TENANT -> "<a class=\"button\" href=\"/listings\">Искать жильё</a>";
            case ADMIN -> "<a class=\"button\" href=\"/admin\">Открыть админку</a>";
        };
        Html.page(exchange, user, title, """
                <section class="narrow">
                    <div class="notice success">
                        <h1>%s</h1>
                        <p>%s, вы вошли как %s. Теперь доступны личный профиль, заявки и действия по вашей роли.</p>
                    </div>
                    <div class="actions">
                        %s
                        <a class="button ghost" href="/profile">Профиль</a>
                        <a class="button ghost" href="/">На главную</a>
                    </div>
                </section>
                """.formatted(Html.escape(title), Html.escape(user.getName()), user.getRole().title(), roleAction));
    }
}
