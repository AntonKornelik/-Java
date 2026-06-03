package ru.renthome.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import ru.renthome.entity.User;

public class UserRepository {
    private final Storage storage;

    public UserRepository(Storage storage) {
        this.storage = storage;
    }

    public User save(User user) {
        storage.state().users.add(user);
        storage.save();
        return user;
    }

    public long nextId() {
        return storage.state().nextUserId++;
    }

    public Optional<User> findById(long id) {
        return storage.state().users.stream().filter(user -> user.getId() == id).findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return storage.state().users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<User> findAll() {
        return storage.state().users.stream()
                .sorted(Comparator.comparingLong(User::getId))
                .toList();
    }
}
