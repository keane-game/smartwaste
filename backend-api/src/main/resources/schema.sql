-- =============================================================================
-- ⚠️  NE PAS EXÉCUTER AUTOMATIQUEMENT — SCRIPT DESTRUCTEUR
--
-- Ce fichier commence par `DROP DATABASE` : l'exécuter au démarrage détruirait la base.
-- Il n'est plus référencé par la configuration (P2-4) et `spring.sql.init.mode: never`
-- est positionné explicitement dans `application.yml`.
--
-- Historique : `application.yml` le déclarait via `spring.datasource.schema` +
-- `initialization-mode: always`, deux propriétés de Spring Boot 1.x qui n'existent plus
-- en Boot 3 — elles étaient donc ignorées en silence. C'est la SEULE raison pour laquelle
-- la base a survécu. Ne pas « corriger » ces clés vers `spring.sql.init.*`.
--
-- Le schéma est géré exclusivement par Liquibase (config/liquibase/master.xml, ADR-0001).
-- Conservé uniquement comme mémo d'amorçage manuel d'un poste de développement.
-- =============================================================================

DROP DATABASE IF EXISTS sonaged;
CREATE DATABASE sonaged;