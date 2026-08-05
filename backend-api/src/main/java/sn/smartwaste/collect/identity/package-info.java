/**
 * Module <b>Identité &amp; Accès</b> (support).
 *
 * <p>Authentification, comptes utilisateurs, activation, rôles et autorités. Entité cible :
 * {@code UserEntity}.
 *
 * <p>Découplage (ADR-0012) : les autres modules référencent l'utilisateur par {@code userId},
 * jamais par association objet, afin qu'une extraction ultérieure de ce contexte n'oblige pas à
 * réécrire leur schéma.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Identité & Accès")
package sn.smartwaste.collect.identity;
