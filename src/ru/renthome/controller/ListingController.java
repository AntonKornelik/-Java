package ru.renthome.controller;

import com.sun.net.httpserver.HttpExchange;
import java.util.Map;
import ru.renthome.entity.Listing;
import ru.renthome.entity.ListingStatus;
import ru.renthome.entity.Review;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.service.FavoriteService;
import ru.renthome.service.ListingService;
import ru.renthome.service.ReviewService;
import ru.renthome.util.Html;
import ru.renthome.util.RequestUtil;
import ru.renthome.util.SessionManager;

public class ListingController extends BaseController {
    private final ListingService listings;
    private final ReviewService reviews;
    private final FavoriteService favorites;

    public ListingController(ListingService listings, ReviewService reviews, FavoriteService favorites, SessionManager sessions) {
        super(sessions);
        this.listings = listings;
        this.reviews = reviews;
        this.favorites = favorites;
    }

    @Override
    protected void handle(HttpExchange exchange, User user) throws Exception {
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/new")) {
            form(exchange, user, null);
        } else if (path.endsWith("/create") && "POST".equals(exchange.getRequestMethod())) {
            create(exchange, user);
        } else if (path.endsWith("/edit")) {
            form(exchange, user, listings.require(Long.parseLong(RequestUtil.query(exchange).get("id"))));
        } else if (path.endsWith("/update") && "POST".equals(exchange.getRequestMethod())) {
            update(exchange, user);
        } else if (path.endsWith("/status") && "POST".equals(exchange.getRequestMethod())) {
            status(exchange, user);
        } else if (path.endsWith("/review") && "POST".equals(exchange.getRequestMethod())) {
            review(exchange, user);
        } else if (path.endsWith("/show")) {
            show(exchange, user);
        } else {
            index(exchange, user);
        }
    }

    private void index(HttpExchange exchange, User user) throws Exception {
        Map<String, String> q = RequestUtil.query(exchange);
        StringBuilder cards = new StringBuilder();
        for (Listing listing : listings.search(RequestUtil.value(q, "city"), RequestUtil.optionalInt(q, "maxPrice"),
                RequestUtil.optionalInt(q, "rooms"), RequestUtil.value(q, "type"), false)) {
            cards.append(ListingViews.card(listing, listings.ownerName(listing), user, favorites.contains(user, listing.getId())));
        }
        Html.page(exchange, user, "Объявления", """
                <section>
                    <h1>Объявления</h1>
                    <form class="filters" method="get">
                        <input name="city" placeholder="Город" value="%s">
                        <input name="maxPrice" type="number" placeholder="Цена до" value="%s">
                        <input name="rooms" type="number" placeholder="Комнат" value="%s">
                        <input name="type" placeholder="Тип" value="%s">
                        <button class="button">Найти</button>
                    </form>
                    <div class="grid">%s</div>
                </section>
                """.formatted(Html.escape(RequestUtil.value(q, "city")), Html.escape(RequestUtil.value(q, "maxPrice")),
                Html.escape(RequestUtil.value(q, "rooms")), Html.escape(RequestUtil.value(q, "type")), cards));
    }

    private void show(HttpExchange exchange, User user) throws Exception {
        long id = Long.parseLong(RequestUtil.query(exchange).get("id"));
        Listing listing = listings.require(id);
        StringBuilder reviewList = new StringBuilder();
        for (Review review : reviews.forListing(id)) {
            reviewList.append("<div class=\"review\"><strong>").append(review.getRating()).append("/5 · ")
                    .append(Html.escape(reviews.authorName(review))).append("</strong><p>")
                    .append(Html.escape(review.getText())).append("</p></div>");
        }
        String booking = user != null && user.getRole() == Role.TENANT ? """
                <form class="form side-form" action="/bookings/create" method="post">
                    <h3>Отправить заявку</h3>
                    <input type="hidden" name="listingId" value="%d">
                    <label>Заезд<input type="date" name="dateFrom" required></label>
                    <label>Выезд<input type="date" name="dateTo" required></label>
                    <label>Жильцов<input type="number" name="guests" value="1" min="1"></label>
                    <label>Комментарий<textarea name="comment" rows="3"></textarea></label>
                    <button class="button full">Отправить</button>
                </form>
                """.formatted(id) : "";
        String edit = user != null && (user.getRole() == Role.ADMIN || user.getId() == listing.getOwnerId())
                ? "<a class=\"button ghost\" href=\"/listings/edit?id=%d\">Редактировать</a>".formatted(id) : "";
        String reviewForm = user != null && user.getRole() == Role.TENANT ? """
                <form class="form" action="/listings/review" method="post">
                    <input type="hidden" name="listingId" value="%d">
                    <label>Оценка<input type="number" name="rating" min="1" max="5" value="5"></label>
                    <label>Отзыв<textarea name="text" rows="3" required></textarea></label>
                    <button class="button">Добавить отзыв</button>
                </form>
                """.formatted(id) : "";
        Html.page(exchange, user, listing.getTitle(), """
                <section class="details">
                    <img class="details-image" src="%s" alt="">
                    <div>
                        <p class="eyebrow">%s · %s</p>
                        <h1>%s</h1>
                        <p class="price">%d BYN/мес</p>
                        <p>%s</p>
                        <p class="muted">%s, %s · %d м² · %d комн. · статус: %s</p>
                        %s
                    </div>
                    %s
                </section>
                <section class="narrow">
                    <h2>Отзывы</h2>
                    %s
                    %s
                </section>
                """.formatted(Html.escape(listing.getImageUrl()), Html.escape(listing.getCity()),
                Html.escape(listing.getType()), Html.escape(listing.getTitle()), listing.getPrice(),
                Html.escape(listing.getDescription()), Html.escape(listing.getCity()), Html.escape(listing.getAddress()),
                listing.getArea(), listing.getRooms(), listing.getStatus().title(), edit, booking,
                reviewList.isEmpty() ? "<p class=\"muted\">Пока отзывов нет.</p>" : reviewList, reviewForm));
    }

    private void form(HttpExchange exchange, User user, Listing listing) throws Exception {
        requireLogin(user);
        String action = listing == null ? "/listings/create" : "/listings/update";
        String hidden = listing == null ? "" : "<input type=\"hidden\" name=\"id\" value=\"" + listing.getId() + "\">";
        Html.page(exchange, user, listing == null ? "Новое объявление" : "Редактирование", """
                <section class="narrow">
                    <h1>%s</h1>
                    <form class="form" action="%s" method="post">
                        %s
                        <label>Название<input name="title" value="%s" required></label>
                        <label>Город<input name="city" value="%s" required></label>
                        <label>Адрес<input name="address" value="%s"></label>
                        <label>Тип<input name="type" value="%s"></label>
                        <label>Цена BYN<input type="number" name="price" value="%d" min="1"></label>
                        <label>Площадь<input type="number" name="area" value="%d" min="1"></label>
                        <label>Комнат<input type="number" name="rooms" value="%d" min="1"></label>
                        <label>Фото URL<input name="imageUrl" value="%s"></label>
                        <label>Описание<textarea name="description" rows="5">%s</textarea></label>
                        <button class="button">Сохранить</button>
                    </form>
                </section>
                """.formatted(listing == null ? "Новое объявление" : "Редактирование", action, hidden,
                val(listing, "title"), val(listing, "city"), val(listing, "address"), val(listing, "type"),
                listing == null ? 100 : listing.getPrice(), listing == null ? 30 : listing.getArea(),
                listing == null ? 1 : listing.getRooms(), val(listing, "imageUrl"), val(listing, "description")));
    }

    private void create(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        Map<String, String> f = RequestUtil.form(exchange);
        Listing listing = listings.create(user, RequestUtil.value(f, "title"), RequestUtil.value(f, "city"),
                RequestUtil.value(f, "address"), RequestUtil.value(f, "type"), RequestUtil.intValue(f, "price", 0),
                RequestUtil.intValue(f, "area", 0), RequestUtil.intValue(f, "rooms", 0),
                RequestUtil.value(f, "description"), RequestUtil.value(f, "imageUrl"));
        Html.redirect(exchange, "/listings/show?id=" + listing.getId());
    }

    private void update(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        Map<String, String> f = RequestUtil.form(exchange);
        listings.update(user, RequestUtil.longValue(f, "id"), RequestUtil.value(f, "title"), RequestUtil.value(f, "city"),
                RequestUtil.value(f, "address"), RequestUtil.value(f, "type"), RequestUtil.intValue(f, "price", 0),
                RequestUtil.intValue(f, "area", 0), RequestUtil.intValue(f, "rooms", 0),
                RequestUtil.value(f, "description"), RequestUtil.value(f, "imageUrl"));
        Html.redirect(exchange, "/listings/show?id=" + RequestUtil.value(f, "id"));
    }

    private void status(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        Map<String, String> f = RequestUtil.form(exchange);
        listings.setStatus(user, RequestUtil.longValue(f, "listingId"), ListingStatus.valueOf(RequestUtil.value(f, "status")));
        Html.redirect(exchange, "/admin");
    }

    private void review(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        Map<String, String> f = RequestUtil.form(exchange);
        reviews.add(user, RequestUtil.longValue(f, "listingId"), RequestUtil.intValue(f, "rating", 5), RequestUtil.value(f, "text"));
        Html.redirect(exchange, "/listings/show?id=" + RequestUtil.value(f, "listingId"));
    }

    private String val(Listing listing, String field) {
        if (listing == null) {
            return "";
        }
        return switch (field) {
            case "title" -> Html.escape(listing.getTitle());
            case "city" -> Html.escape(listing.getCity());
            case "address" -> Html.escape(listing.getAddress());
            case "type" -> Html.escape(listing.getType());
            case "imageUrl" -> Html.escape(listing.getImageUrl());
            case "description" -> Html.escape(listing.getDescription());
            default -> "";
        };
    }
}
