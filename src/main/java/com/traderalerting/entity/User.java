package com.traderalerting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Pour l'enum
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
public class User {

    public enum AuthProvider {
        LOCAL, GOOGLE // Ajoutez d'autres providers si besoin (Facebook, etc.)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Garder unique=true si vous voulez un seul compte par username
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank // Garder si username toujours requis
    @Size(min = 3, max = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank
    @Email
    private String email;

    @Column(nullable = true) // <-- RENDRE NULLABLE (ou gérer autrement)
    private String password; // Null pour les utilisateurs Google

    @Column(unique = true, nullable = true) // ID unique fourni par Google
    private String googleId;

    @NotNull // Important d'avoir un provider
    @Enumerated(EnumType.STRING) // Stocke l'enum comme String (LOCAL, GOOGLE)
    @Column(nullable = false)
    private AuthProvider authProvider;

    // Garder ce constructeur si utile pour les tests ou autre
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.authProvider = AuthProvider.LOCAL; // Par défaut LOCAL pour ce constructeur
    }

    // Constructeur spécifique pour Google (exemple)
    public User(String username, String email, String googleId, AuthProvider provider) {
        this.username = username;
        this.email = email;
        this.googleId = googleId;
        this.authProvider = provider;
        this.password = null; // Pas de mot de passe local
    }
}