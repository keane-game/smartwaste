# =============================================================================
# Image applicative Smart Collect / SONAGED (ADR-0019, priorité 10 du brief produit).
#
# Deux étages : on compile dans une image qui contient Maven et le JDK, on n'expédie que le
# JRE et le jar. L'image finale ne contient donc ni sources, ni dépôt Maven, ni compilateur —
# ce qui réduit d'autant la surface exposée si le conteneur est compromis.
# =============================================================================

# --- Étage 1 : construction ---------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Le POM est copié SEUL, et les dépendances résolues, avant les sources. Docker met en cache
# chaque instruction : tant que `pom.xml` ne change pas, cette couche est réutilisée et le
# téléchargement des dépendances (plusieurs minutes) n'est pas rejoué à chaque modification
# de code. Copier tout d'un bloc invaliderait le cache au moindre caractère changé.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src

# Tests ignorés ICI, volontairement : une image se construit hors ligne, alors que la suite
# exige un PostgreSQL joignable (`ddl-auto: validate` confronte les entités au schéma
# Liquibase). Les tests tournent en intégration continue, où la base est fournie
# (.github/workflows/backend.yml). Construire l'image n'est pas le moment de les valider.
RUN mvn -B -q -DskipTests package

# --- Étage 2 : exécution ------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# L'application ne tourne PAS en root. Un processus root dans le conteneur reste root sur les
# volumes montés — ici `datas/`, monté depuis le dépôt de l'hôte.
RUN addgroup -S smartwaste && adduser -S -G smartwaste smartwaste

WORKDIR /app
COPY --from=build --chown=smartwaste:smartwaste /build/target/*.jar /app/application.jar

USER smartwaste
EXPOSE 8089

# `MaxRAMPercentage` plutôt qu'un `-Xmx` fixe : la JVM lit alors la limite mémoire du
# conteneur (cgroup) au lieu de celle de la machine hôte. Sans cela, une JVM à qui l'on
# accorde 512 Mo croit disposer de toute la RAM de l'hôte et se fait tuer par l'OOM killer
# au premier pic — panne classique, et illisible depuis les journaux applicatifs.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

# Forme exec (pas de shell) : le processus Java devient PID 1 et reçoit SIGTERM directement,
# ce qui permet l'arrêt propre de Spring (fermeture du pool de connexions, des flux SSE).
# En forme shell, le signal irait au shell et l'arrêt se terminerait par un SIGKILL après
# le délai de grâce.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/application.jar"]
