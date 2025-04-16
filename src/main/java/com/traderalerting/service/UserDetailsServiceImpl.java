package com.traderalerting.service;

import com.traderalerting.entity.User; // Votre entité User
import com.traderalerting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // Pour les rôles/autorités (vide pour l'instant)

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Autowired 
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository; // Injection via le constructeur
    }

    @Override
    @Transactional(readOnly = true) // Important pour les opérations de lecture liées à JPA
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Trouver l'utilisateur par username OU email
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username) // Essaie aussi par email
                        .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username or email: " + username)));

        // Convertir votre entité User en UserDetails de Spring Security
        // Le troisième argument est la liste des autorités (rôles), vide pour le moment
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // Le mot de passe HACHÉ de la base de données
                new ArrayList<>() // Liste vide d'autorités pour commencer
        );
    }
}