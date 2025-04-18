package com.traderalerting.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "favorites")
public class User {
    
    public enum AuthProvider {
        LOCAL, GOOGLE
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = true)
    private String password;
    
    @Column(unique = true, nullable = true)
    private String googleId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;
    
    // Relation ManyToMany pour les favoris
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_favorites",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "symbol_id")
    )
    private Set<Symbol> favorites = new HashSet<>();
    
    // Constructeur pour inscription locale
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.authProvider = AuthProvider.LOCAL;
    }
    
    // Constructeur pour OAuth
    public User(String username, String email, String googleId, AuthProvider provider) {
        this.username = username;
        this.email = email;
        this.googleId = googleId;
        this.authProvider = provider;
    }
    
    // Méthodes helper pour la gestion des favoris
    public void addFavorite(Symbol symbol) {
        this.favorites.add(symbol);
    }
    
    public void removeFavorite(Symbol symbol) {
        this.favorites.remove(symbol);
    }
}