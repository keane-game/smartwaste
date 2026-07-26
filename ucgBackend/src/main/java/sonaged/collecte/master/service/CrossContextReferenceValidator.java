package sonaged.collecte.master.service;

import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.repository.CommuneRepository;
import sonaged.collecte.master.repository.QuartierRepository;

/**
 * Validation applicative des références <strong>cross-contexte</strong> (P1-7 / ADR-0012).
 *
 * <p>Les frontières de contexte interdisent les FK physiques traversantes : un dépotoir
 * (contexte « Point de collecte ») ne peut pas contraindre en SQL une commune ou un quartier
 * (contexte « Référentiel territorial »). L'intégrité est donc reportée ici, au niveau
 * applicatif, comme le prévoit l'ADR-0012 :
 *
 * <blockquote>« l'intégrité cross-contexte est assurée par validation applicative,
 * pas par contrainte SQL »</blockquote>
 *
 * <p>Ce composant est le point unique où le contexte appelant interroge le contexte
 * propriétaire. Le jour où le référentiel territorial devient un microservice (ADR-0010),
 * seules ces méthodes changent — l'appel au repository est remplacé par un appel réseau —
 * sans toucher aux services métier appelants.
 *
 * <p><strong>Limite assumée</strong> : la vérification n'est pas transactionnellement liée à
 * l'écriture. Une commune supprimée juste après la validation laisse une référence orpheline
 * (cohérence éventuelle). C'est le compromis explicitement accepté par l'ADR-0012.
 */
@Service
public class CrossContextReferenceValidator {

    private final CommuneRepository communeRepository;
    private final QuartierRepository quartierRepository;

    public CrossContextReferenceValidator(CommuneRepository communeRepository,
                                          QuartierRepository quartierRepository) {
        this.communeRepository = communeRepository;
        this.quartierRepository = quartierRepository;
    }

    /** Vérifie qu'une commune référencée existe. {@code null} est accepté (référence optionnelle). */
    public void requireCommuneExists(Long communeId) {
        if (communeId != null && !communeRepository.existsById(communeId)) {
            throw new ResourceNotFoundException(
                    "Commune référencée [%s] introuvable (référentiel territorial)".formatted(communeId));
        }
    }

    /** Vérifie qu'un quartier référencé existe. {@code null} est accepté (référence optionnelle). */
    public void requireQuartierExists(Long quartierId) {
        if (quartierId != null && !quartierRepository.existsById(quartierId)) {
            throw new ResourceNotFoundException(
                    "Quartier référencé [%s] introuvable (référentiel territorial)".formatted(quartierId));
        }
    }
}
