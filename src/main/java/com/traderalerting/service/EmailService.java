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
        String descriptionAlerte;

        switch (alerteId) {
            case 1:
                descriptionAlerte = "Variation supérieure à 7.5% par rapport au dernier mois.";
                break;
            case 2:
                descriptionAlerte = "Formation d'une bougie particulière détectée.";
                break;
            case 3:
                descriptionAlerte = "Croisement de moyennes mobiles détecté.";
                break;
            case 4:
                descriptionAlerte = "Augmentation de 3% par rapport à la veille.";
                break;
            case 5:
                descriptionAlerte = "Franchissement d’un seuil technique.";
                break;
            default:
                descriptionAlerte = "Type d'alerte inconnu.";
                break;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Alerte boursière - " + nomCours);
        message.setText("Bonjour,\n\nUne alerte a été déclenchée sur le cours : " 
            + nomCours + ".\n" 
            + "Type d'alerte : " + descriptionAlerte + "\n"
            + "ID de l'alerte : " + alerteId + ".\n\n"
            + "Cordialement,\nVotre application de suivi boursier.");
        message.setFrom("ton.email@gmail.com");

        mailSender.send(message);
    }
}
