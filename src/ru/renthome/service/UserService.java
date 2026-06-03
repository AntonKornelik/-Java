package ru.renthome.service;

import java.util.List;
import ru.renthome.entity.Role;
import ru.renthome.entity.User;
import ru.renthome.repository.Storage;
import ru.renthome.repository.UserRepository;
import ru.renthome.util.PasswordUtil;

public class UserService {
    private final UserRepository users;
    private final Storage storage;

    public UserService(UserRepository users, Storage storage) {
        this.users = users;
        this.storage = storage;
    }

    public User register(String name, String email, String password, Role role) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Заполните имя, email и пароль");
        }
        if (users.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        User user = new User(users.nextId(), name.trim(), email.trim().toLowerCase(), "", "",
                PasswordUtil.hash(password), role);
        return users.save(user);
    }

    public User login(String email, String password) {
        User user = users.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));
        if (user.isBlocked()) {
            throw new IllegalArgumentException("Аккаунт заблокирован администратором");
        }
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }
        return user;
    }

    public User require(long id) {
        return users.findById(id).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    public List<User> all() {
        return users.findAll();
    }

    public void updateProfile(User user, String name, String phone, String about) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Имя не должно быть пустым");
        }
        user.setName(name.trim());
        user.setPhone(phone.trim());
        user.setAbout(about.trim());
        storage.save();
    }

    public void toggleBlocked(long userId) {
        User user = require(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Администратора нельзя заблокировать");
        }
        user.setBlocked(!user.isBlocked());
        storage.save();
    }
}
