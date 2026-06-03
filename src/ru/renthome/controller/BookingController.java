package ru.renthome.controller;

import com.sun.net.httpserver.HttpExchange;
import java.util.Map;
import ru.renthome.entity.Booking;
import ru.renthome.entity.BookingStatus;
import ru.renthome.entity.Listing;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.service.BookingService;
import ru.renthome.service.ListingService;
import ru.renthome.util.Html;
import ru.renthome.util.RequestUtil;
import ru.renthome.util.SessionManager;

public class BookingController extends BaseController {
    private final BookingService bookings;
    private final ListingService listings;

    public BookingController(BookingService bookings, ListingService listings, SessionManager sessions) {
        super(sessions);
        this.bookings = bookings;
        this.listings = listings;
    }

    @Override
    protected void handle(HttpExchange exchange, User user) throws Exception {
        requireLogin(user);
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/create") && "POST".equals(exchange.getRequestMethod())) {
            create(exchange, user);
        } else if (path.endsWith("/status") && "POST".equals(exchange.getRequestMethod())) {
            status(exchange, user);
        } else {
            index(exchange, user);
        }
    }

    private void index(HttpExchange exchange, User user) throws Exception {
        StringBuilder rows = new StringBuilder();
        for (Booking booking : bookings.forUser(user)) {
            Listing listing = listings.require(booking.getListingId());
            String actions = user.getRole() == Role.OWNER || user.getRole() == Role.ADMIN ? """
                    <form class="inline" action="/bookings/status" method="post">
                        <input type="hidden" name="bookingId" value="%d">
                        <button name="status" value="APPROVED">Принять</button>
                        <button name="status" value="REJECTED">Отклонить</button>
                    </form>
                    """.formatted(booking.getId()) : "";
            rows.append("""
                    <tr>
                        <td><a href="/listings/show?id=%d">%s</a></td>
                        <td>%s - %s</td>
                        <td>%d</td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(listing.getId(), Html.escape(listing.getTitle()), booking.getDateFrom(),
                    booking.getDateTo(), booking.getGuests(), booking.getStatus().title(), actions));
        }
        Html.page(exchange, user, "Заявки", """
                <section>
                    <h1>Заявки</h1>
                    <div class="table-wrap">
                        <table>
                            <thead><tr><th>Жильё</th><th>Даты</th><th>Жильцов</th><th>Статус</th><th></th></tr></thead>
                            <tbody>%s</tbody>
                        </table>
                    </div>
                </section>
                """.formatted(rows.isEmpty() ? "<tr><td colspan=\"5\">Заявок пока нет</td></tr>" : rows));
    }

    private void create(HttpExchange exchange, User user) throws Exception {
        Map<String, String> f = RequestUtil.form(exchange);
        bookings.create(user, RequestUtil.longValue(f, "listingId"), RequestUtil.dateValue(f, "dateFrom"),
                RequestUtil.dateValue(f, "dateTo"), RequestUtil.intValue(f, "guests", 1), RequestUtil.value(f, "comment"));
        Html.redirect(exchange, "/bookings");
    }

    private void status(HttpExchange exchange, User user) throws Exception {
        Map<String, String> f = RequestUtil.form(exchange);
        bookings.changeStatus(user, RequestUtil.longValue(f, "bookingId"), BookingStatus.valueOf(RequestUtil.value(f, "status")));
        Html.redirect(exchange, "/bookings");
    }
}
