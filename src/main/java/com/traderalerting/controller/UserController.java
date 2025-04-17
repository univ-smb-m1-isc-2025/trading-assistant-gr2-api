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

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api") // Base path for API endpoints


public class UserController {

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
}
