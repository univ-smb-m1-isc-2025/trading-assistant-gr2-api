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
    public String sendEmail(
            @RequestParam String email,
            @RequestParam String nomCours,
            @RequestParam int alerteId
    ) {
        emailService.sendAlerteMail(email, nomCours, alerteId);
        return "Email envoyé avec succès à " + email;
    }
}