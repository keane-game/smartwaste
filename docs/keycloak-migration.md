# Migration vers Keycloak (P0-A / ADR-0011) — blueprint

> Statut : **AJOURNÉ** (2026-07-29, cf. ADR-0011). Aucune bascule effectuée. Ce document reste la
> référence d'implémentation **le jour où la bascule est décidée**.
>
> ⚠️ **Ne pas appliquer le §4 en l'état.** Il prévoit de supprimer `JwtService`, `JwtFilter` et
> `SecurityConstants` — or ces classes portent désormais les **sessions révocables**, qui sont la
> solution d'authentification en vigueur. Les supprimer aujourd'hui retirerait la révocation sans
> rien mettre à la place.
> Contrainte de la session courante : pas de JDK → code de référence **non compilé/non vérifié**.
> Étapes marquées 🔧 = code à ajouter ; ⛔ = code à retirer ; 🖥️ = infra ; 📱 = clients.

## Principe
Le backend cesse de fabriquer des JWT et devient un **Resource Server** validant les tokens signés par
Keycloak (JWKS). Comptes, mots de passe, activation e-mail, MFA, reset → gérés par Keycloak.

## 🖥️ 1. Keycloak (infra)
- Lancer Keycloak (Docker) et créer un realm `sonaged`.
- Clients : `sonaged-web` (public, Authorization Code + PKCE), `sonaged-mobile` (public, PKCE).
- Rôles de realm : `ADMIN`, `SUPERVISEUR`, `AGENT`, `CITOYEN` (mapper depuis l'enum `Permission` existant).
- Exporter le realm et le versionner (`ucgBackend/src/main/resources/keycloak/realm-sonaged.json`).

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
- Angular : `angular-oauth2-oidc` (Code + PKCE), intercepteur ajoutant le Bearer.
- Flutter : `flutter_appauth` (PKCE), stockage sécurisé du token.

## 👥 7. Migration des comptes
Importer les utilisateurs existants dans Keycloak (import LDIF/JSON ou script Admin API) ;
forcer une réinitialisation de mot de passe (les mots de passe locaux ne sont pas réutilisables).

## Vérification (à faire avec JDK + Keycloak)
1. Démarrer Keycloak + `-Dspring.profiles.active=keycloak`.
2. Obtenir un token via le client, appeler un endpoint protégé → 200 ; sans token → 401.
3. Vérifier le mapping des rôles (`ROLE_ADMIN`…) sur un endpoint sécurisé.
