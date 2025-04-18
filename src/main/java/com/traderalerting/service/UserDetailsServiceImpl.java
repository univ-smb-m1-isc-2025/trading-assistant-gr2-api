package com.traderalerting.service;

import com.traderalerting.entity.User;
import com.traderalerting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; 

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Autowired 
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository; 
    }

    @Override
    @Transactional(readOnly = true) 
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Trouver l'utilisateur par username OU email
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username) 
                        .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username or email: " + username)));

        // Convertir l'entité User en UserDetails de Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // Le mot de passe HACHÉ de la base de données
                new ArrayList<>() 
        );
    }
}