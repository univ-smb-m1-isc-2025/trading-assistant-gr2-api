package com.traderalerting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendAlerteMail(String toEmail, String nomCours, int alerteId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Alerte boursière - " + nomCours);
        message.setText("Bonjour,\n\nUne alerte a été déclenchée sur le cours : " 
            + nomCours + ".\nID de l'alerte : " + alerteId + ".\n\nCordialement,\nVotre application de suivi boursier.");
        message.setFrom("ton.email@gmail.com");

        mailSender.send(message);
    }
}
