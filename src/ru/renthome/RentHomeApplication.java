package ru.renthome;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import ru.renthome.controller.AdminController;
import ru.renthome.controller.AuthController;
import ru.renthome.controller.BookingController;
import ru.renthome.controller.FavoriteController;
import ru.renthome.controller.ListingController;
import ru.renthome.controller.PageController;
import ru.renthome.controller.StaticController;
import ru.renthome.repository.BookingRepository;
import ru.renthome.repository.FavoriteRepository;
import ru.renthome.repository.ListingRepository;
import ru.renthome.repository.ReviewRepository;
import ru.renthome.repository.Storage;
import ru.renthome.repository.UserRepository;
import ru.renthome.service.BookingService;
import ru.renthome.service.FavoriteService;
import ru.renthome.service.ListingService;
import ru.renthome.service.ReviewService;
import ru.renthome.service.UserService;
import ru.renthome.util.SessionManager;

public class RentHomeApplication {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8090;
        Storage storage = new Storage();
        UserRepository users = new UserRepository(storage);
        ListingRepository listings = new ListingRepository(storage);
        BookingRepository bookings = new BookingRepository(storage);
        FavoriteRepository favorites = new FavoriteRepository(storage);
        ReviewRepository reviews = new ReviewRepository(storage);

        UserService userService = new UserService(users, storage);
        ListingService listingService = new ListingService(listings, users, storage);
        BookingService bookingService = new BookingService(bookings, listings, storage);
        FavoriteService favoriteService = new FavoriteService(favorites, listings, storage);
        ReviewService reviewService = new ReviewService(reviews, listings, users, storage);
        SessionManager sessions = new SessionManager(users);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new PageController(userService, listingService, favoriteService, sessions));
        server.createContext("/auth", new AuthController(userService, sessions));
        server.createContext("/listings", new ListingController(listingService, reviewService, favoriteService, sessions));
        server.createContext("/bookings", new BookingController(bookingService, listingService, sessions));
        server.createContext("/favorites", new FavoriteController(favoriteService, sessions));
        server.createContext("/admin", new AdminController(userService, listingService, sessions));
        server.createContext("/assets", new StaticController());
        server.setExecutor(null);
        server.start();
        System.out.println("RentHome started: http://localhost:" + port);
        new CountDownLatch(1).await();
    }
}
