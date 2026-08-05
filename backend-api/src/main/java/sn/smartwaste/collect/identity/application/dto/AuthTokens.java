package sn.smartwaste.collect.identity.application.dto;

/**
 * Couple de jetons rendu à la connexion et au rafraîchissement.
 *
 * @param bearer  jeton d'accès (JWT signé, courte durée souhaitable), porte le {@code sid}
 * @param refresh jeton de rafraîchissement opaque — à conserver comme un secret et à présenter
 *                sur {@code POST /auth/refresh}
 */
public record AuthTokens(String bearer, String refresh) { }
