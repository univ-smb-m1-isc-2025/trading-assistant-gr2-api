package com.traderalerting.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Important pour que le filtre soit un bean géré par Spring
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService; // Utilisez l'interface ici

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Si pas d'en-tête Authorization ou s'il ne commence pas par "Bearer ", on passe au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrait le token (après "Bearer ")
        jwt = authHeader.substring(7);
        try {
             username = jwtService.extractUsername(jwt);

             // Si on a le username et que l'utilisateur n'est pas déjà authentifié dans le contexte actuel
             if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                 UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                 // Si le token est valide
                 if (jwtService.isTokenValid(jwt, userDetails)) {
                     // Créer un objet Authentication
                     UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                             userDetails,
                             null, // Pas besoin de credentials ici car on se base sur le token
                             userDetails.getAuthorities()
                     );
                     // Ajouter les détails de la requête web
                     authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                     // Mettre à jour le SecurityContextHolder (authentifier l'utilisateur pour cette requête)
                     SecurityContextHolder.getContext().setAuthentication(authToken);
                 }
             }
             filterChain.doFilter(request, response);
        } catch (Exception e) {
            // En cas d'erreur de parsing ou de validation du token, ne pas authentifier et laisser la chaîne continuer
            // On pourrait logger l'erreur ici
            logger.warn("Could not process JWT token: " + e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}