package ru.renthome.controller;

import com.sun.net.httpserver.HttpExchange;
import java.util.Map;
import ru.renthome.entity.Listing;
import ru.renthome.entity.ListingStatus;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.service.ListingService;
import ru.renthome.service.UserService;
import ru.renthome.util.Html;
import ru.renthome.util.RequestUtil;
import ru.renthome.util.SessionManager;

public class AdminController extends BaseController {
    private final UserService users;
    private final ListingService listings;

    public AdminController(UserService users, ListingService listings, SessionManager sessions) {
        super(sessions);
        this.users = users;
        this.listings = listings;
    }

    @Override
    protected void handle(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Раздел доступен только администратору");
        }
        if (exchange.getRequestURI().getPath().endsWith("/block") && "POST".equals(exchange.getRequestMethod())) {
            Map<String, String> form = RequestUtil.form(exchange);
            users.toggleBlocked(RequestUtil.longValue(form, "userId"));
            Html.redirect(exchange, "/admin");
            return;
        }
        index(exchange, user);
    }

    private void index(HttpExchange exchange, User user) throws Exception {
        StringBuilder userRows = new StringBuilder();
        for (User item : users.all()) {
            userRows.append("""
                    <tr>
                        <td>%s</td><td>%s</td><td>%s</td><td>%s</td>
                        <td>
                            <form class="inline" action="/admin/block" method="post">
                                <input type="hidden" name="userId" value="%d">
                                <button>%s</button>
                            </form>
                        </td>
                    </tr>
                    """.formatted(Html.escape(item.getName()), Html.escape(item.getEmail()), item.getRole().title(),
                    item.isBlocked() ? "Заблокирован" : "Активен", item.getId(),
                    item.isBlocked() ? "Разблокировать" : "Заблокировать"));
        }
        StringBuilder listingRows = new StringBuilder();
        for (Listing listing : listings.search("", null, null, "", true)) {
            listingRows.append("""
                    <tr>
                        <td><a href="/listings/show?id=%d">%s</a></td><td>%s</td><td>%s</td>
                        <td>
                            <form class="inline" action="/listings/status" method="post">
                                <input type="hidden" name="listingId" value="%d">
                                <select name="status">%s%s%s</select>
                                <button>Сохранить</button>
                            </form>
                        </td>
                    </tr>
                    """.formatted(listing.getId(), Html.escape(listing.getTitle()), Html.escape(listing.getCity()),
                    listing.getStatus().title(), listing.getId(),
                    Html.option("ACTIVE", "Активно", listing.getStatus().name()),
                    Html.option("HIDDEN", "Скрыто", listing.getStatus().name()),
                    Html.option("DELETED", "Удалено", listing.getStatus().name())));
        }
        Html.page(exchange, user, "Админка", """
                <section>
                    <h1>Администрирование</h1>
                    <h2>Пользователи</h2>
                    <div class="table-wrap"><table>
                        <thead><tr><th>Имя</th><th>Email</th><th>Роль</th><th>Статус</th><th></th></tr></thead>
                        <tbody>%s</tbody>
                    </table></div>
                    <h2>Объявления</h2>
                    <div class="table-wrap"><table>
                        <thead><tr><th>Название</th><th>Город</th><th>Статус</th><th>Модерация</th></tr></thead>
                        <tbody>%s</tbody>
                    </table></div>
                </section>
                """.formatted(userRows, listingRows));
    }
}
