package sonaged.collecte.master.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sonaged.collecte.master.repository.SoftDeleteRepository;
import sonaged.collecte.master.service.SoftDeleteService;

import java.util.List;

/**
 * Purge planifiée des ressources soft-deletées dont le délai de rétention est dépassé.
 *
 * <p>Couvre <b>toutes</b> les entités : Spring injecte ici l'ensemble des beans
 * {@link SoftDeleteRepository} (tout repository l'étendant). Nécessite {@code @EnableScheduling}
 * (activé sur {@code SonagedApplication}). Fréquence : {@code sonaged.deletion.purge-cron}
 * (par défaut tous les jours à 03:00).
 */
@Component
@Slf4j
public class DeletionPurgeScheduler {

    private final List<SoftDeleteRepository<?, ?>> repositories;
    private final SoftDeleteService softDeleteService;

    public DeletionPurgeScheduler(List<SoftDeleteRepository<?, ?>> repositories,
                                  SoftDeleteService softDeleteService) {
        this.repositories = repositories;
        this.softDeleteService = softDeleteService;
    }

    @Scheduled(cron = "${sonaged.deletion.purge-cron:0 0 3 * * *}")
    public void purgeExpiredDeletions() {
        int total = 0;
        for (SoftDeleteRepository<?, ?> repository : repositories) {
            total += softDeleteService.purgeExpired(repository);
        }
        if (total > 0) {
            log.info("Purge quotidienne du soft-delete : {} enregistrement(s) supprimé(s) définitivement.", total);
        }
    }
}
