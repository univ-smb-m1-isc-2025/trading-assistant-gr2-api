package com.traderalerting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.traderalerting.service.EmailService;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public String sendEmail(@RequestBody EmailRequest request) {
        emailService.sendAlerteMail(request.getEmail(), request.getNomCours(), request.getAlerteId());
        return "Email envoyé avec succès à " + request.getEmail();
    }
    
    // Classe interne pour représenter la requête
    private static class EmailRequest {
        private String email;
        private String nomCours;
        private int alerteId;
        
        // Getters et setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNomCours() { return nomCours; }
        public void setNomCours(String nomCours) { this.nomCours = nomCours; }
        public int getAlerteId() { return alerteId; }
        public void setAlerteId(int alerteId) { this.alerteId = alerteId; }
    }
}