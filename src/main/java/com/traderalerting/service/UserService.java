package com.traderalerting.service;


import com.traderalerting.dto.UserRegistrationDto;
import com.traderalerting.registration.exception.UserAlreadyExistsException;
import com.traderalerting.entity.User;
import com.traderalerting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired // Constructor injection is preferred
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional // Ensure the operation is atomic
    public User registerNewUser(UserRegistrationDto registrationDto) {
        // Check if username already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + registrationDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
             throw new UserAlreadyExistsException("Email already registered: " + registrationDto.getEmail());
        }

        // Create new user entity
        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setEmail(registrationDto.getEmail());

        // Encode the password before saving
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        // Save the user to the database
        return userRepository.save(newUser);
    }
}
