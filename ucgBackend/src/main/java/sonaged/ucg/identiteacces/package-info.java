/**
 * Module <b>Identité &amp; Accès</b> (support).
 *
 * <p>Authentification déléguée à <b>Keycloak</b> (OIDC, ADR-0011) ; profil utilisateur local lié au
 * {@code sub} Keycloak ; mapping des rôles Keycloak → autorités. Entité cible : {@code UserEntity}.
 *
 * <p>Découplage (ADR-0012) : les autres modules référencent l'utilisateur par {@code userId}
 * (identifiant), jamais par association objet. Communication par événements ({@code UserRegistered}…).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Identité & Accès")
package sonaged.ucg.identiteacces;
