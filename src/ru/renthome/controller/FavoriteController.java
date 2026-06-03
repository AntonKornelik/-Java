package ru.renthome.controller;

import com.sun.net.httpserver.HttpExchange;
import java.util.Map;
import ru.renthome.entity.Listing;
import ru.renthome.entity.User;
import ru.renthome.service.FavoriteService;
import ru.renthome.util.Html;
import ru.renthome.util.RequestUtil;
import ru.renthome.util.SessionManager;

public class FavoriteController extends BaseController {
    private final FavoriteService favorites;

    public FavoriteController(FavoriteService favorites, SessionManager sessions) {
        super(sessions);
        this.favorites = favorites;
    }

    @Override
    protected void handle(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        if (exchange.getRequestURI().getPath().endsWith("/toggle") && "POST".equals(exchange.getRequestMethod())) {
            Map<String, String> form = RequestUtil.form(exchange);
            favorites.toggle(user, RequestUtil.longValue(form, "listingId"));
            String back = exchange.getRequestHeaders().getFirst("Referer");
            Html.redirect(exchange, back != null && !back.isBlank() ? back : "/listings");
            return;
        }
        StringBuilder cards = new StringBuilder();
        for (Listing listing : favorites.forUser(user)) {
            cards.append(ListingViews.card(listing, "", user, true));
        }
        Html.page(exchange, user, "Избранное", """
                <section>
                    <h1>Избранное</h1>
                    <div class="grid">%s</div>
                </section>
                """.formatted(cards.isEmpty() ? "<p class=\"muted\">Пока ничего не добавлено.</p>" : cards));
    }
}
