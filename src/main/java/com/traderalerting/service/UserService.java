package com.traderalerting.service;

// --- Imports existants ---
import com.traderalerting.dto.UserRegistrationDto;
import com.traderalerting.exception.UserAlreadyExistsException;
import com.traderalerting.entity.User;
import com.traderalerting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID; // Pour générer un mot de passe aléatoire si besoin

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final com.traderalerting.security.JwtService jwtService; 

    @Value("${google.client.id}") // Injectez l'ID client Google
    private String googleClientId;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, com.traderalerting.security.JwtService jwtService,UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // --- Méthode pour l'inscription locale (existante) ---
    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) throws UserAlreadyExistsException {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
             throw new UserAlreadyExistsException("Username already taken: " + registrationDto.getUsername());
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
             throw new UserAlreadyExistsException("Email already registered: " + registrationDto.getEmail());
        }

        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setEmail(registrationDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setAuthProvider(User.AuthProvider.LOCAL); 

        return userRepository.save(newUser);
    }

    @Transactional
    public String processGoogleToken(String googleIdTokenString) throws Exception {
        // 1. Vérifier le token Google
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(googleIdTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google ID token.");
        }

        // 2. Extraire les informations
        GoogleIdToken.Payload payload = idToken.getPayload();
        String userId = payload.getSubject(); // ID Google unique de l'utilisateur
        String email = payload.getEmail();
        boolean emailVerified = payload.getEmailVerified();
        String name = (String) payload.get("name");
        // String pictureUrl = (String) payload.get("picture");
        // String locale = (String) payload.get("locale");
        // String familyName = (String) payload.get("family_name");
        // String givenName = (String) payload.get("given_name");

        if (!emailVerified) {
            throw new IllegalArgumentException("Google email not verified.");
        }

        // 3. Trouver ou Créer l'utilisateur local
        // On cherche d'abord par Google ID, puis par Email pour lier les comptes
        Optional<User> userOptional = userRepository.findByGoogleId(userId);
        if (userOptional.isEmpty()) {
             userOptional = userRepository.findByEmail(email);
        }

        User user;
        if (userOptional.isPresent()) {
            // L'utilisateur existe déjà (par email ou googleId)
            user = userOptional.get();
            // Mettre à jour les infos si nécessaire (ex: lier googleId si trouvé par email)
            if (user.getGoogleId() == null) {
                user.setGoogleId(userId);
            }
            user.setAuthProvider(User.AuthProvider.GOOGLE);
            user = userRepository.save(user);
        } else {
            // Nouvel utilisateur
            user = new User();
            user.setEmail(email);
            user.setGoogleId(userId);
            user.setAuthProvider(User.AuthProvider.GOOGLE);
            user.setUsername(generateUniqueUsername(email, name));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            user = userRepository.save(user);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return jwtService.generateToken(userDetails);
    }

    // --- Méthode utilitaire pour générer un username unique (exemple basique) ---
    private String generateUniqueUsername(String email, String name) {
        String baseUsername = email.split("@")[0]; 
        if (name != null && !name.trim().isEmpty()) {
            baseUsername = name.replaceAll("\\s+", "").toLowerCase(); // le nom sans espace
        }

        String finalUsername = baseUsername;
        int counter = 1;
        // Vérifie si le username existe déjà et ajoute un numéro si c'est le cas
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + counter;
            counter++;
        }
        return finalUsername;
    }

    @Transactional
    public void deleteUserAccount(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
        
        // D'abord supprimer les relations (favoris)
        jdbcTemplate.update("DELETE FROM user_favorites WHERE user_id = ?", user.getId());
        
        // Puis supprimer l'utilisateur
        userRepository.delete(user);
    }
}