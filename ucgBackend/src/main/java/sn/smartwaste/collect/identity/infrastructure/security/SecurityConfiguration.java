package sn.smartwaste.collect.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
// Sans @EnableMethodSecurity, les annotations @PreAuthorize sont ignorees EN SILENCE : le code
// paraitrait protege et ne le serait pas. C'est deja le defaut des beans SecurityRule de ce
// module, qui declarent 13 regles d'autorisation que rien n'applique.
@EnableMethodSecurity
public class SecurityConfiguration{
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDetailsService userDetailsService;

    private final JwtFilter jwtFilter;
    public SecurityConfiguration(BCryptPasswordEncoder bCryptPasswordEncoder, JwtFilter jwtFilter, UserDetailsService userDetailsService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return
                httpSecurity

                        .cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration cors = new CorsConfiguration();
                                cors.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                                cors.setAllowedMethods(Collections.singletonList("*"));
                                cors.setAllowCredentials(true);
                                cors.setAllowedHeaders(Collections.singletonList("*"));
                                cors.setExposedHeaders(Collections.singletonList("Authorization"));
                                cors.setMaxAge(3600L);
                                return cors;
                            }
                        }))
                        .csrf(AbstractHttpConfigurer::disable)
                        .authorizeHttpRequests(
                                authorize ->
                                        authorize
                                                .requestMatchers("/swagger-ui/**", "/sonaged-docs/**", "/error", "/").permitAll()
                                                .requestMatchers(POST,"/auth/").permitAll()
                                                .requestMatchers(POST,"/auth/**").permitAll()
                                                // 🔴 `/data/**` était ouvert TOUTES MÉTHODES CONFONDUES.
                                                // Or ce préfixe ne sert pas qu'aux compteurs publics du
                                                // tableau de bord : `DashboardController` y expose aussi
                                                // sept `POST /data/{commune|department|quartier|circuit…}`
                                                // qui écrivent en base des GeoJSON de référence. Autrement
                                                // dit, n'importe qui, sans jeton, pouvait injecter ou
                                                // écraser le référentiel territorial et les points de
                                                // collecte. Seule la LECTURE reste publique ; l'écriture
                                                // retombe sur `anyRequest().authenticated()`.
                                                // Non régressif pour le frontend : `JwtInterceptor` place
                                                // le jeton sur toutes les requêtes, et l'écran d'import
                                                // n'est atteignable qu'authentifié.
                                                .requestMatchers(GET, "/data/**").permitAll()
                                                // Ingestion IoT (ADR-0004) : un capteur n'est pas
                                                // une personne. Il s'authentifie par une cle de
                                                // device (en-tete X-Device-Key) que le service
                                                // verifie lui-meme, et repond 401 si elle est
                                                // inconnue. Lui distribuer un JWT utilisateur
                                                // reviendrait a donner a un objet pose dans la rue
                                                // les droits d'un compte.
                                                .requestMatchers(POST, "/v1/measurements").permitAll()
                                                // Meme raison pour les traceurs embarques.
                                                .requestMatchers(POST, "/v1/vehicle-positions").permitAll()
                                                .anyRequest().authenticated()
                        )
                        .sessionManagement(httpSecuritySessionManagementConfigurer ->
                                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                                )
                        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                        .build();
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider () {
        // Spring Security 6.5 : le constructeur sans argument et setUserDetailsService(...) sont
        // dépréciés — le UserDetailsService se fournit désormais par le constructeur.
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return daoAuthenticationProvider;
    }

}
