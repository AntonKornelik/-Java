package ru.renthome.repository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import ru.renthome.entity.Listing;
import ru.renthome.entity.Review;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.util.PasswordUtil;

public class Storage {
    private final Path path = Path.of("data", "renthome.ser");
    private DataState state;

    public Storage() {
        this.state = load();
        seedIfEmpty();
        seedReviewsIfEmpty();
        save();
    }

    public synchronized DataState state() {
        return state;
    }

    public synchronized void save() {
        try {
            Files.createDirectories(path.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))) {
                out.writeObject(state);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить данные", e);
        }
    }

    private DataState load() {
        if (!Files.exists(path)) {
            return new DataState();
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
            return (DataState) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new DataState();
        }
    }

    private void seedIfEmpty() {
        if (!state.users.isEmpty()) {
            return;
        }
        User admin = new User(state.nextUserId++, "Администратор", "admin@renthome.local", "+375 29 000-00-00",
                "Модерация сервиса", PasswordUtil.hash("admin"), Role.ADMIN);
        User owner = new User(state.nextUserId++, "Антон Владелец", "owner@renthome.local", "+375 29 111-22-33",
                "Сдаю аккуратные квартиры в Минске", PasswordUtil.hash("owner"), Role.OWNER);
        User tenant = new User(state.nextUserId++, "Артём Арендатор", "tenant@renthome.local", "+375 29 444-55-66",
                "Ищу жильё рядом с метро", PasswordUtil.hash("tenant"), Role.TENANT);
        state.users.add(admin);
        state.users.add(owner);
        state.users.add(tenant);

        state.listings.add(new Listing(state.nextListingId++, owner.getId(), "Светлая студия у метро",
                "Минск", "пр-т Независимости, 89", "Студия", 980, 34, 1,
                "Уютная студия с быстрым интернетом, рабочим местом и всей бытовой техникой.",
                "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=80"));
        state.listings.add(new Listing(state.nextListingId++, owner.getId(), "Двухкомнатная квартира в центре",
                "Минск", "ул. Немига, 12", "Квартира", 1450, 58, 2,
                "Подходит для пары или семьи. Есть парковка, посудомойка и просторная гостиная.",
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80"));
        state.listings.add(new Listing(state.nextListingId++, owner.getId(), "Комната рядом с университетом",
                "Гомель", "ул. Советская, 47", "Комната", 420, 18, 1,
                "Бюджетный вариант для студента. Рядом остановки, магазины и парк.",
                "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=1200&q=80"));
    }

    private void seedReviewsIfEmpty() {
        if (!state.reviews.isEmpty() || state.listings.isEmpty()) {
            return;
        }
        User maria = ensureTenant("Мария Соколова", "maria@renthome.local", "+375 29 210-43-11",
                "Часто снимаю жильё на короткие поездки");
        User pavel = ensureTenant("Павел Ковалёв", "pavel@renthome.local", "+375 33 904-12-77",
                "Ищу спокойные варианты рядом с транспортом");
        User alina = ensureTenant("Алина Мороз", "alina@renthome.local", "+375 44 330-88-20",
                "Люблю чистые квартиры с рабочим местом");
        User nikita = ensureTenant("Никита Романов", "nikita@renthome.local", "+375 25 118-55-42",
                "Снимаю жильё для учёбы и командировок");

        String[] texts = {
                "Квартира полностью совпала с описанием, заселение прошло быстро и без лишних вопросов.",
                "Понравилось расположение и чистота. До транспорта несколько минут пешком.",
                "Хороший вариант за свою цену, владелец отвечает быстро и подробно.",
                "Внутри тихо, техника работает, интернет стабильный. Для учебы и работы удобно.",
                "Фотографии честные, район спокойный, рядом есть магазины и кафе.",
                "Оставались на несколько дней, всё прошло нормально. Особенно понравилась кухня.",
                "Жильё аккуратное, постель и полотенца были подготовлены заранее.",
                "Немного не хватило места для вещей, но в остальном вариант удачный.",
                "Удобная планировка и приятный ремонт. Повторно рассмотрел бы этот объект.",
                "Цена адекватная, коммуникация с владельцем без проблем."
        };
        User[] authors = {maria, pavel, alina, nikita};
        int index = 0;
        for (Listing listing : state.listings) {
            for (int i = 0; i < 4; i++) {
                User author = authors[(index + i) % authors.length];
                int rating = 4 + ((index + i) % 2);
                state.reviews.add(new Review(state.nextReviewId++, listing.getId(), author.getId(), rating,
                        texts[(index + i) % texts.length]));
            }
            index += 3;
        }
    }

    private User ensureTenant(String name, String email, String phone, String about) {
        return state.users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseGet(() -> {
                    User user = new User(state.nextUserId++, name, email, phone, about,
                            PasswordUtil.hash("tenant"), Role.TENANT);
                    state.users.add(user);
                    return user;
                });
    }
}
