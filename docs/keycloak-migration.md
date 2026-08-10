# Migration vers Keycloak (P0-A / ADR-0011) — blueprint

> Statut : **REPRISE DÉCIDÉE** (2026-08-09, cf. ADR-0011 §Décision de reprise). L'ajournement du
> 2026-07-29 est levé, mais **aucune bascule n'est encore effectuée** — ce document reste la
> référence d'implémentation, à exécuter une fois le préalable ci-dessous vérifié.
>
> ⚠️ **Préalable non vérifié avant toute étape de ce blueprint** : confirmer qu'un démon Docker est
> réellement disponible dans l'environnement où Keycloak doit tourner. `backend-api/src/main/resources/docker-compose.yml`
> déclare des services, mais `docs/IMPLEMENTATION_LOG.md` (2026-08-06) note encore l'absence de démon
> Docker dans les sessions où ce fichier a été écrit/étendu — l'existence du fichier ne prouve pas
> qu'il tourne. C'était le blocage opérationnel qui avait motivé l'ajournement ; il doit être
> explicitement revérifié, pas supposé résolu.
>
> ⚠️ **Ne pas appliquer le §4 en l'état.** Il prévoit de supprimer `JwtService`, `JwtFilter` et
> `SecurityConstants` — or ces classes portent désormais les **sessions révocables**, qui restent la
> solution d'authentification en vigueur **jusqu'à ce que le resource server Keycloak soit validé en
> conditions réelles** (étape de vérification en bas de page). Les supprimer avant cette validation
> retirerait la révocation sans rien mettre à la place.
> Le code de référence du §3 reste **non compilé/non vérifié** — un JDK est disponible depuis (voir
> `docs/IMPLEMENTATION_LOG.md`), mais personne n'a tenté de compiler ce blueprint spécifique tant
> que la bascule elle-même n'était pas décidée. C'est désormais fait (décision), mais la vérification
> technique reste à faire.
>
> **Coordination avec ADR-0020** (cloisonnement multi-tenant) : `OrganizationMembership.userId`
> référence l'identifiant `UserEntity` **local**, pas le `sub` Keycloak. Le §5 ci-dessous (profil
> local conservé, résolu depuis `sub`) rend les deux chantiers indépendants — ADR-0020 n'a rien à
> changer quand cette bascule aura lieu.
>
> **En attendant** : `docs/adr/0021-completion-api-identite-pont-keycloak.md` couvre les correctifs
> et endpoints pont (reset/changement de mot de passe, désactivation de compte) à traiter côté auth
> maison pendant que ce chantier se prépare — explicitement destinés à disparaître une fois ce
> blueprint exécuté.
>
> Étapes marquées 🔧 = code à ajouter ; ⛔ = code à retirer ; 🖥️ = infra ; 📱 = clients.

## Principe
Le backend cesse de fabriquer des JWT et devient un **Resource Server** validant les tokens signés par
Keycloak (JWKS). Comptes, mots de passe, activation e-mail, MFA, reset → gérés par Keycloak.

## 🖥️ 1. Keycloak (infra)
- Lancer Keycloak (Docker) et créer un realm `sonaged`.
- Clients : `sonaged-web` (public, Authorization Code + PKCE), `sonaged-mobile` (public, PKCE).
- Rôles de realm : `ADMIN`, `SUPERVISEUR`, `AGENT`, `CITOYEN` (mapper depuis l'enum `Permission` existant).
- Exporter le realm et le versionner (`backend-api/src/main/resources/keycloak/realm-sonaged.json`).

## 🔧 2. Dépendance (fait)
`spring-boot-starter-oauth2-resource-server` ajoutée au `pom.xml` (inerte sans issuer-uri).

## 🔧 3. Configuration Resource Server (à créer, gardée par profil `keycloak`)
Fichier cible : `security/keycloak/KeycloakResourceServerConfig.java` (à activer une fois Keycloak prêt) :

```java
@Configuration
@Profile("keycloak")
public class KeycloakResourceServerConfig {

    @Bean
    SecurityFilterChain keycloakFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/sonaged-docs/**", "/error", "/").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(converter())));
        return http.build();
    }

    // Keycloak place les rôles dans realm_access.roles → autorités ROLE_*
    private JwtAuthenticationConverter converter() {
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realm = jwt.getClaim("realm_access");
            if (realm == null || realm.get("roles") == null) return List.of();
            Collection<String> roles = (Collection<String>) realm.get("roles");
            return roles.stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
        });
        return c;
    }
}
```
Config du profil : `application-keycloak.properties` (déjà créé, `issuer-uri` via `KEYCLOAK_ISSUER_URI`).

## ⛔ 4. Code maison à retirer (une fois la bascule validée)
- `security/JwtService.java`, `security/JwtFilter.java`, `constant/SecurityConstants.java`
- `security/SecurityConfiguration.java` (ou la neutraliser hors profil `keycloak`)
- Minting de token + gestion mot de passe dans `AuthServiceImpl` / `AuthController`
- Codes d'activation e-mail (`ValidationService`, `NotificationServiceImpl.sendCodeOfValidation`)

## 🔧 5. Profil utilisateur local (découplage identité/domaine, ADR-0012)
Conserver un `UserEntity` **profil** référencé par le `sub` Keycloak (`keycloakSub`), sans mot de passe.
Créer/rapprocher le profil à la première requête authentifiée (claim `sub`).

## 📱 6. Clients
- Angular (`sonaged_web/`, renommé depuis `angular/` le 2026-08-06) : `angular-oauth2-oidc` (Code + PKCE), intercepteur ajoutant le Bearer.
- Flutter (`mobileFlutter/`) : `flutter_appauth` (PKCE), stockage sécurisé du token.

## 👥 7. Migration des comptes
Importer les utilisateurs existants dans Keycloak (import LDIF/JSON ou script Admin API) ;
forcer une réinitialisation de mot de passe (les mots de passe locaux ne sont pas réutilisables).

## Vérification (à faire avec JDK + Keycloak)
1. Démarrer Keycloak + `-Dspring.profiles.active=keycloak`.
2. Obtenir un token via le client, appeler un endpoint protégé → 200 ; sans token → 401.
3. Vérifier le mapping des rôles (`ROLE_ADMIN`…) sur un endpoint sécurisé.
