package com.traderalerting.config; // Gardez votre nom de package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
// Supprimez l'import de WebMvcConfigurer si vous ne l'utilisez plus pour autre chose
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// Plus besoin d'implémenter WebMvcConfigurer pour CETTE configuration CORS
public class CorsConfig /* implements WebMvcConfigurer */ {

    @Bean
    public CorsFilter corsFilter() {
        // Créer une configuration CORS
        CorsConfiguration corsConfig = new CorsConfiguration();

        // --- Adapter les origines ici ---
        corsConfig.addAllowedOrigin("http://localhost:5173");          // Pour vos tests locaux frontend
        corsConfig.addAllowedOrigin("https://www.beRich.oups.net");     // L'URL de VOTRE frontend déployé (HTTPS!)
        // Remplacez par les URLs exactes si elles sont différentes

        // Autoriser les méthodes nécessaires (incluant OPTIONS pour preflight)
        corsConfig.addAllowedMethod("*"); // Ou listez-les explicitement : GET, POST, PUT, DELETE, OPTIONS

        // Autoriser tous les en-têtes
        corsConfig.addAllowedHeader("*");

        // Autoriser les credentials (cookies, en-tête Authorization)
        corsConfig.setAllowCredentials(true);

        // Appliquer la configuration à toutes les URLs ("/**")
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        System.out.println("CORS Filter Bean configuration applied."); // Log pour vérifier

        return new CorsFilter(source); // Créer et retourner le bean CorsFilter
    }

    // Si vous aviez d'AUTRES configurations dans addCorsMappings ou ailleurs dans
    // cette classe en tant que WebMvcConfigurer, il faudra les déplacer
    // ou les gérer autrement. Sinon, vous pouvez supprimer 'implements WebMvcConfigurer'.
}