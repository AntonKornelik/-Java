package ru.renthome.entity;

import java.io.Serializable;

public class User implements Serializable {
    private final long id;
    private String name;
    private String email;
    private String phone;
    private String about;
    private String passwordHash;
    private Role role;
    private boolean blocked;

    public User(long id, String name, String email, String phone, String about, String passwordHash, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.about = about;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAbout() { return about; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isBlocked() { return blocked; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAbout(String about) { this.about = about; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(Role role) { this.role = role; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
