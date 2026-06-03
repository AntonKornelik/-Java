package ru.renthome.controller;

import com.sun.net.httpserver.HttpExchange;
import java.util.Map;
import ru.renthome.entity.Listing;
import ru.renthome.entity.User;
import ru.renthome.service.FavoriteService;
import ru.renthome.service.ListingService;
import ru.renthome.service.UserService;
import ru.renthome.util.Html;
import ru.renthome.util.RequestUtil;
import ru.renthome.util.SessionManager;

public class PageController extends BaseController {
    private final UserService users;
    private final ListingService listings;
    private final FavoriteService favorites;

    public PageController(UserService users, ListingService listings, FavoriteService favorites, SessionManager sessions) {
        super(sessions);
        this.users = users;
        this.listings = listings;
        this.favorites = favorites;
    }

    @Override
    protected void handle(HttpExchange exchange, User user) throws Exception {
        String path = exchange.getRequestURI().getPath();
        if ("/profile".equals(path)) {
            profile(exchange, user);
            return;
        }
        if ("/profile/edit".equals(path)) {
            editProfile(exchange, user);
            return;
        }
        if ("/profile/update".equals(path) && "POST".equals(exchange.getRequestMethod())) {
            updateProfile(exchange, user);
            return;
        }
        home(exchange, user);
    }

    private void home(HttpExchange exchange, User user) throws Exception {
        StringBuilder cards = new StringBuilder();
        for (Listing listing : listings.search("", null, null, "", false).stream().limit(3).toList()) {
            cards.append(ListingViews.card(listing, listings.ownerName(listing), user, favorites.contains(user, listing.getId())));
        }

        String primaryAction;
        String secondaryAction;
        String panel;
        if (user == null) {
            primaryAction = "<a class=\"button\" href=\"/listings\">Смотреть жильё</a>";
            secondaryAction = "<a class=\"button ghost\" href=\"/auth/register\">Создать аккаунт</a>";
            panel = """
                    <strong>Демо-вход</strong>
                    <span>admin@renthome.local / admin</span>
                    <span>owner@renthome.local / owner</span>
                    <span>tenant@renthome.local / tenant</span>
                    """;
        } else {
            primaryAction = switch (user.getRole()) {
                case OWNER -> "<a class=\"button\" href=\"/listings/new\">Добавить объявление</a>";
                case TENANT -> "<a class=\"button\" href=\"/listings\">Искать жильё</a>";
                case ADMIN -> "<a class=\"button\" href=\"/admin\">Админка</a>";
            };
            secondaryAction = "<a class=\"button ghost\" href=\"/bookings\">Мои заявки</a>";
            panel = """
                    <strong>Вы вошли</strong>
                    <span>%s</span>
                    <span>%s</span>
                    <a class="button small" href="/profile">Открыть профиль</a>
                    """.formatted(Html.escape(user.getName()), user.getRole().title());
        }

        Html.page(exchange, user, "Главная", """
                <section class="hero">
                    <div>
                        <p class="eyebrow">Курсовой проект по Java</p>
                        <h1>Сервис аренды жилья</h1>
                        <p>Поиск квартир, публикация объявлений, заявки на аренду, избранное, отзывы и модерация в одном учебном web-приложении.</p>
                        <div class="actions">
                            %s
                            %s
                        </div>
                    </div>
                    <div class="hero-panel">%s</div>
                </section>
                <section>
                    <div class="section-head">
                        <h2>Популярные варианты</h2>
                        <a href="/listings">Все объявления</a>
                    </div>
                    <div class="grid">%s</div>
                </section>
                """.formatted(primaryAction, secondaryAction, panel, cards));
    }

    private void profile(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        boolean saved = "1".equals(RequestUtil.value(RequestUtil.query(exchange), "saved"));
        String savedNotice = saved ? "<div class=\"notice success\">Профиль сохранён</div>" : "";

        Html.page(exchange, user, "Профиль", """
                <section class="narrow">
                    <h1>Профиль</h1>
                    %s
                    <div class="profile-card">
                        <div class="avatar">%s</div>
                        <div>
                            <p class="eyebrow">%s</p>
                            <h2>%s</h2>
                            <p class="muted">%s</p>
                        </div>
                    </div>
                    <dl class="profile-data">
                        <div><dt>Email</dt><dd>%s</dd></div>
                        <div><dt>Телефон</dt><dd>%s</dd></div>
                        <div><dt>О себе</dt><dd>%s</dd></div>
                    </dl>
                    <div class="actions">
                        <a class="button" href="/profile/edit">Редактировать</a>
                        <a class="button ghost" href="/listings">К объявлениям</a>
                    </div>
                </section>
                """.formatted(savedNotice, initials(user.getName()), user.getRole().title(), Html.escape(user.getName()),
                Html.escape(user.getEmail()), Html.escape(user.getEmail()), valueOrEmpty(user.getPhone()), valueOrEmpty(user.getAbout())));
    }

    private void editProfile(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        Html.page(exchange, user, "Редактирование профиля", """
                <section class="narrow">
                    <h1>Редактирование профиля</h1>
                    <form class="form" action="/profile/update" method="post">
                        <label>Имя<input name="name" value="%s" required></label>
                        <label>Телефон<input name="phone" value="%s"></label>
                        <label>О себе<textarea name="about" rows="4">%s</textarea></label>
                        <p class="muted">Роль: %s. Email: %s</p>
                        <div class="actions">
                            <button class="button" type="submit">Сохранить</button>
                            <a class="button ghost" href="/profile">Отмена</a>
                        </div>
                    </form>
                </section>
                """.formatted(Html.escape(user.getName()), Html.escape(user.getPhone()), Html.escape(user.getAbout()),
                user.getRole().title(), Html.escape(user.getEmail())));
    }

    private void updateProfile(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        Map<String, String> form = RequestUtil.form(exchange);
        users.updateProfile(user, RequestUtil.value(form, "name"), RequestUtil.value(form, "phone"),
                RequestUtil.value(form, "about"));
        Html.redirect(exchange, "/profile?saved=1");
    }

    private String valueOrEmpty(String value) {
        return value == null || value.isBlank() ? "<span class=\"muted\">Не указано</span>" : Html.escape(value);
    }

    private String initials(String name) {
        String value = name == null || name.isBlank() ? "U" : name.trim().substring(0, 1);
        return Html.escape(value.toUpperCase());
    }
}
