package pe.edu.upeu.PharmaBackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
public class CorsConfig {

    private final String[] origenesPermitidos;

    public CorsConfig(
            @Value("${app.cors.allowed-origins:"
                    + "http://localhost:5173,"
                    + "http://127.0.0.1:5173,"
                    + "http://localhost:4200,"
                    + "http://localhost:3000}")
            String[] origenesPermitidos) {

        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origenesPermitidos));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Content-Type", "Accept", "Authorization", "Origin"));
        config.setExposedHeaders(Arrays.asList("Location"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
