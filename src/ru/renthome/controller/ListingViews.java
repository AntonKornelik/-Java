package ru.renthome.controller;

import ru.renthome.entity.Listing;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.util.Html;

final class ListingViews {
    private ListingViews() {
    }

    static String card(Listing listing, String owner, User user, boolean favorite) {
        String fav = user != null && user.getRole() == Role.TENANT
                ? """
                <form action="/favorites/toggle" method="post">
                    <input type="hidden" name="listingId" value="%d">
                    <button class="icon-button %s" title="Избранное">%s</button>
                </form>
                """.formatted(listing.getId(), favorite ? "active" : "", favorite ? "★" : "☆")
                : "";
        return """
                <article class="card">
                    <img src="%s" alt="">
                    <div class="card-body">
                        <div class="card-top">
                            <h3>%s</h3>
                            %s
                        </div>
                        <p class="muted">%s, %s</p>
                        <p>%d BYN/мес · %d м² · %d комн. · %s</p>
                        <p class="muted">Владелец: %s</p>
                        <a class="button full" href="/listings/show?id=%d">Подробнее</a>
                    </div>
                </article>
                """.formatted(Html.escape(listing.getImageUrl()), Html.escape(listing.getTitle()), fav,
                Html.escape(listing.getCity()), Html.escape(listing.getAddress()), listing.getPrice(),
                listing.getArea(), listing.getRooms(), Html.escape(listing.getType()), Html.escape(owner), listing.getId());
    }
}
