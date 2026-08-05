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

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebSecurity
// Sans @EnableMethodSecurity, les annotations @PreAuthorize sont ignorees EN SILENCE : le code
// paraitrait protege et ne le serait pas. C'est deja le defaut des beans SecurityRule de ce
// module, qui declarent 13 regles d'autorisation que rien n'applique.
@EnableMethodSecurity
public class SecurityConfiguration{

    /**
     * Rôles habilités à administrer. Ce sont ceux qui <b>existent réellement</b> (semés par le
     * changelog 2.1.0) : le modèle ne connaît ni agent ni superviseur.
     *
     * <p>Sans préfixe {@code ROLE_} : {@code hasAnyRole} l'ajoute, et
     * {@code UserEntity.getAuthorities()} le pose déjà côté principal.
     */
    private static final String[] ADMINISTRATION = { "ADMIN", "SUPER_ADMIN" };

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
                                                // `/swagger-ui.html` est listé À PART de `/swagger-ui/**` : c'est un
                                                // chemin distinct, pas un fichier du répertoire. springdoc l'expose
                                                // comme porte d'entrée (valeur par défaut de
                                                // `springdoc.swagger-ui.path`) et le redirige vers
                                                // `/swagger-ui/index.html`. Le motif `/swagger-ui/**` ne le couvre
                                                // donc pas, et la redirection était refusée en 403 avant d'avoir lieu
                                                // — l'UI n'était atteignable qu'en visant `/swagger-ui/index.html`.
                                                .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                                        "/sonaged-docs/**", "/error", "/").permitAll()
                                                // Sonde de disponibilité (ADR-0019). Ouverte parce qu'un
                                                // orchestrateur interroge le conteneur AVANT que quiconque
                                                // puisse s'authentifier — le compte d'amorçage n'existe pas
                                                // encore quand la première sonde part.
                                                //
                                                // `/actuator/health` SEUL, jamais `/actuator/**` : les autres
                                                // points d'Actuator publient la configuration, les variables
                                                // d'environnement et les beans — c'est-à-dire l'inventaire
                                                // exact d'où chercher les secrets. `show-details: never`
                                                // (application.yml) réduit en outre la réponse à UP/DOWN :
                                                // le détail nommerait la base et son état.
                                                .requestMatchers(GET, "/actuator/health",
                                                        "/actuator/health/**").permitAll()
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

                                                // =========================================================
                                                //  AUTORISATION PAR RÔLE
                                                //
                                                //  Jusqu'ici la chaîne s'arrêtait à
                                                //  `anyRequest().authenticated()` : **tout compte
                                                //  authentifié pouvait tout faire**. Or `/auth/register`
                                                //  est public — n'importe qui pouvait donc créer un compte
                                                //  et, une fois activé, écraser le référentiel territorial,
                                                //  réimporter les GeoJSON, créer d'autres comptes ou vider
                                                //  la corbeille. Les 13 règles de `AuthorityRules` /
                                                //  `UserRules` donnaient l'illusion du contraire : rien ne
                                                //  les applique.
                                                //
                                                //  L'ordre compte : la première règle qui correspond gagne.
                                                // =========================================================

                                                // ---- Gestes d'habitant : déclarés AVANT le refus par
                                                // défaut, sinon un citoyen ne pourrait plus rien déposer.
                                                .requestMatchers(POST, "/avis").authenticated()
                                                .requestMatchers(GET, "/avis/mine").authenticated()
                                                .requestMatchers("/v1/collection-subscriptions",
                                                                 "/v1/collection-subscriptions/**").authenticated()

                                                // ---- Administration : la LECTURE aussi est réservée.
                                                // La liste des comptes, l'état de la corbeille et les
                                                // rapports de supervision renseignent sur l'organisation
                                                // autant que les écritures la modifient.
                                                .requestMatchers("/v1/users*", "/v1/users/**",
                                                                 "/v1/authorities*", "/v1/authorities/**",
                                                                 "/v1/deletions", "/v1/deletions/**",
                                                                 "/v1/admin/**",
                                                                 "/v1/supervision/**").hasAnyRole(ADMINISTRATION)
                                                // Le flux SSE diffuse toutes les alertes de la ville.
                                                .requestMatchers(GET, "/v1/alerts/stream").hasAnyRole(ADMINISTRATION)

                                                // ---- Écriture : refusée par défaut.
                                                //
                                                // Énumérer les ressources à protéger serait reproduire
                                                // l'erreur d'origine : celle qu'on oublie ne proteste pas.
                                                // Ici c'est l'inverse — un nouvel endpoint d'écriture sous
                                                // `/v1` est fermé tant que personne ne l'ouvre, et le mode
                                                // de panne devient un 403 visible en développement plutôt
                                                // qu'un trou silencieux.
                                                // G2 : un citoyen enregistre son propre appareil.
                                                // Ecriture sous /v1, donc nommee avant la regle
                                                // generale qui la refuserait — l'identite vient du
                                                // jeton, jamais du corps de la requete.
                                                .requestMatchers(POST, "/v1/device-tokens").authenticated()
                                                .requestMatchers(DELETE, "/v1/device-tokens").authenticated()
                                                .requestMatchers(POST, "/data/**").hasAnyRole(ADMINISTRATION)
                                                // L'agent de collecte déclare ses passages : deux
                                                // écritures, nommées une par une, AVANT la règle
                                                // générale ci-dessous qui les refuserait.
                                                //
                                                // Le `@PreAuthorize` posé sur ces méthodes ne
                                                // suffisait pas : la chaîne de filtres tranche
                                                // avant la sécurité de méthode, si bien que l'agent
                                                // recevait 403 sur SA PROPRE commune. Aucun test
                                                // unitaire ne pouvait le voir — le service et son
                                                // annotation étaient justes, c'est l'ordre des deux
                                                // mécanismes qui ne l'était pas. Trouvé en exerçant
                                                // un vrai compte AGENT contre PostgreSQL.
                                                //
                                                // Le bornage au territoire, lui, reste applicatif
                                                // (`TerritorialAccessGuard`) : une URL ne dit pas
                                                // quelle commune porte le point visé.
                                                .requestMatchers(POST,
                                                        "/v1/collection-routes/stops/*/collected",
                                                        "/v1/collection-routes/stops/*/inaccessible")
                                                        .hasAnyRole("AGENT", "ADMIN", "SUPER_ADMIN")
                                                .requestMatchers(POST, "/v1/**").hasAnyRole(ADMINISTRATION)
                                                .requestMatchers(PUT, "/v1/**").hasAnyRole(ADMINISTRATION)
                                                .requestMatchers(PATCH, "/v1/**").hasAnyRole(ADMINISTRATION)
                                                .requestMatchers(DELETE, "/v1/**").hasAnyRole(ADMINISTRATION)

                                                // ---- Lecture du référentiel : ouverte à tout compte.
                                                // La carte, les quartiers et les horaires de collecte sont
                                                // ce qu'un habitant vient consulter ; les fermer viderait
                                                // l'application mobile de son contenu.
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
