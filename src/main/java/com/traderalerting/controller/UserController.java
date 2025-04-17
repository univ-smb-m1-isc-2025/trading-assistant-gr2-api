package com.traderalerting.controller;

import com.traderalerting.dto.UserRegistrationDto;
import com.traderalerting.exception.UserAlreadyExistsException;
import com.traderalerting.service.UserService;
import com.traderalerting.entity.User;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api") // Base path for API endpoints
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // @Valid triggers validation based on annotations in UserRegistrationDto
        try {
            User registeredUser = userService.registerNewUser(registrationDto);
            // Don't return the password hash in the response!
            // You might return a simplified success message or a DTO without sensitive info.
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully!");
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict
        } catch (Exception e) {
            // Log the exception e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    // --- Exception Handlers for Validation Errors ---
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors; // Returns a map of field -> error message
    }

    @ResponseStatus(HttpStatus.CONFLICT) // 409
    @ExceptionHandler(UserAlreadyExistsException.class)
    public Map<String, String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return error;
    }

    @DeleteMapping("/user/delete")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Delete account request received for user: {}", 
                    userDetails != null ? userDetails.getUsername() : "unknown");
                    
        if (userDetails == null) {
            logger.warn("Delete account attempt with null UserDetails");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non authentifié");
        }

        try {
            logger.info("Attempting to delete account for user: {}", userDetails.getUsername());
            userService.deleteUserAccount(userDetails.getUsername());
            logger.info("Account deleted successfully for user: {}", userDetails.getUsername());
            return ResponseEntity.ok("Compte supprimé avec succès");
        } catch (UsernameNotFoundException e) {
            logger.error("User not found during account deletion: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting account for user: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du compte: " + e.getMessage());
        }
    }
}