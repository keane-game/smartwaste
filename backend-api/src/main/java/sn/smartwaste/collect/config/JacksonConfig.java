package sn.smartwaste.collect.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Empechait Jackson de planter sur les entites JPA qui fuitaient dans un DTO
 * ({@code Department.region}, {@code Department.communes} — gap documente,
 * docs/FRONTEND_API_MAPPING.md) : sans ce module, une relation lazy non initialisee serialise le
 * proxy ByteBuddy genere par Hibernate au lieu de l'entite, et Jackson leve
 * "Type definition error: ... ByteBuddyInterceptor" (500, trouve en verifiant l'app en
 * conditions reelles).
 * <p><b>Correctif racine applique depuis (P1-2, 2026-08-11)</b> : {@code Region}/{@code Department}/
 * {@code Quartier} n'exposent plus d'entites JPA du tout — remplacees par des identifiants
 * ({@code regionId}, {@code communeIds}...), meme patron que {@code Commune.departmentId}. Ce
 * module n'a donc plus de proxy a deballer pour ce cas precis ; conserve comme filet de securite
 * generique si un futur DTO reintroduit une entite par erreur, plutot qu'un 500 opaque.
 * {@link Hibernate6Module} deballe le proxy vers l'entite reelle si initialisee, ecrit {@code null}
 * sinon, plutot que de planter.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Module hibernate6Module() {
        return new Hibernate6Module();
    }
}
