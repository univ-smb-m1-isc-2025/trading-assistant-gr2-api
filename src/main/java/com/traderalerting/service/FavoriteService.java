package com.traderalerting.service;

import com.traderalerting.dto.SymbolDto;
import com.traderalerting.entity.Symbol;
import com.traderalerting.entity.User;
import com.traderalerting.repository.SymbolRepository;
import com.traderalerting.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    
    private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SymbolRepository symbolRepository;
    
    @Transactional(readOnly = true)
    public Set<SymbolDto> getFavorites(String username) {
        log.info("Récupération des favoris pour l'utilisateur: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return user.getFavorites().stream()
            .map(symbol -> new SymbolDto(symbol.getTicker(), symbol.getName()))
            .collect(Collectors.toSet());
    }
    
    @Transactional
    public void addFavorite(String username, String ticker) {
        log.info("Ajout du favori {} pour l'utilisateur {}", ticker, username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        Symbol symbol = symbolRepository.findByTicker(ticker)
            .orElseThrow(() -> new EntityNotFoundException("Symbol not found: " + ticker));
        
        user.addFavorite(symbol);
        userRepository.save(user);
        
        log.info("Favori {} ajouté pour l'utilisateur {}", ticker, username);
    }
    
    @Transactional
    public void removeFavorite(String username, String ticker) {
        log.info("Suppression du favori {} pour l'utilisateur {}", ticker, username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        Symbol symbol = symbolRepository.findByTicker(ticker)
            .orElse(null);
            
        if (symbol != null) {
            user.removeFavorite(symbol);
            userRepository.save(user);
            log.info("Favori {} supprimé pour l'utilisateur {}", ticker, username);
        } else {
            log.warn("Tentative de suppression d'un favori inexistant: {} pour {}", ticker, username);
        }
    }
}