package com.traderalerting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data; // Lombok: Generates getters, setters, toString, etc.
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_users") // "user" might be a reserved keyword in SQL
@Data // Lombok
@NoArgsConstructor // Lombok
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use IDENTITY for Postgres auto-increment
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Password cannot be blank")
    // We don't set a size limit here as the hashed password will be longer
    @Column(nullable = false)
    private String password; // Store the HASHED password

    // Constructors (Lombok generates @NoArgsConstructor, add @AllArgsConstructor if needed)
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password; // Remember this should be the hashed password before saving
    }
}