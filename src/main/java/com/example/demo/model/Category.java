package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Kategoria jest powiązana z użytkownikiem, ale null dla kategorii domyślnych/wbudowanych
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Jeśli user jest null, to jest to kategoria domyślna

    private boolean isDefault = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Standardowy getter dla boolean
    public boolean isDefault() {
        return isDefault;
    }

    // Setter (zmieniona nazwa argumentu na bardziej konwencjonalną)
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    // --- Konstruktory ---

    // Konstruktor używany do tworzenia obiektu z samym ID (dla referencji JPA/Controller)
    public Category(Long id) {
        this.id = id;
    }

    // Konstruktor używany do tworzenia nowych encji (np. w Service)
    public Category(String name, User user, boolean isDefault) {
        this.name = name;
        this.user = user;
        this.isDefault = isDefault;
    }

    // Konstruktor bezargumentowy (wymagany przez JPA)
    public Category() {}
}