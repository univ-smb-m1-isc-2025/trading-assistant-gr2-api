package com.traderalerting.init;

import com.traderalerting.entity.Symbol;
import com.traderalerting.repository.SymbolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SymbolDataInitializer implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(SymbolDataInitializer.class);
    
    @Autowired
    private SymbolRepository symbolRepository;
    
    @Override
    public void run(String... args) {
        log.info("Vérification de l'initialisation des symboles...");
        
        if (symbolRepository.count() == 0) {
            log.info("Aucun symbole trouvé. Initialisation des symboles...");
            
            List<Symbol> symbols = Arrays.asList(
                new Symbol("AIR.PA", "Airbus"),
                new Symbol("AI.PA", "Air Liquide"),
                new Symbol("ACCP.PA", "Accor"),
                new Symbol("AC.PA", "ArcelorMittal"),
                new Symbol("ATOS.PA", "Atos"),
                new Symbol("SU.PA", "Schneider Electric"),
                new Symbol("BNPP.PA", "BNP Paribas"),
                new Symbol("EN.PA", "Bouygues"),
                new Symbol("CAP.PA", "Capgemini"),
                new Symbol("CA.PA", "Carrefour"),
                new Symbol("SGO.PA", "Saint-Gobain"),
                new Symbol("MC.PA", "LVMH"),
                new Symbol("ML.PA", "Michelin"),
                new Symbol("OR.PA", "L'Oréal"),
                new Symbol("ORA.PA", "Orange"),
                new Symbol("PUB.PA", "Publicis"),
                new Symbol("RI.PA", "Pernod Ricard"),
                new Symbol("RNO.PA", "Renault"),
                new Symbol("SAFT.PA", "Safran"),
                new Symbol("SAN.PA", "Sanofi"),
                new Symbol("SGOB.PA", "Société Générale"),
                new Symbol("STM.PA", "STMicroelectronics"),
                new Symbol("TTE.PA", "TotalEnergies"),
                new Symbol("UG.PA", "Unibail-Rodamco-Westfield"),
                new Symbol("VIE.PA", "Veolia"),
                new Symbol("VIV.PA", "Vivendi"),
                new Symbol("DG.PA", "Vinci"),
                new Symbol("WLDGF.PA", "Worldline")
            );
            
            symbolRepository.saveAll(symbols);
            log.info("{} symboles initialisés avec succès.", symbols.size());
        } else {
            log.info("{} symboles déjà présents dans la base de données.", symbolRepository.count());
            
            // Vérifier si AIR.PA existe spécifiquement (car c'est celui qui cause des problèmes)
            boolean airExists = symbolRepository.existsByTicker("AIR.PA");
            log.info("Le symbole AIR.PA existe-t-il? {}", airExists);
            
            if (!airExists) {
                log.warn("Le symbole AIR.PA n'existe pas! Ajout...");
                symbolRepository.save(new Symbol("AIR.PA", "Airbus"));
            }
        }
    }
}