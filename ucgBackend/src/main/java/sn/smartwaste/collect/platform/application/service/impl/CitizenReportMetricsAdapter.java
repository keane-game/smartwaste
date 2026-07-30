package sn.smartwaste.collect.platform.application.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.platform.application.api.CitizenReportMetrics;
import sn.smartwaste.collect.platform.domain.model.AvisStatus;
import sn.smartwaste.collect.platform.domain.repository.AvisRepository;

/** Implémentation des métriques de traitement des signalements. */
@Service
@Transactional(readOnly = true)
public class CitizenReportMetricsAdapter implements CitizenReportMetrics {

    private final AvisRepository avisRepository;

    /** Injectée plutôt qu'appelée en statique : « depuis plus de 48 h » n'est testable qu'à instant figé. */
    private final Clock clock;

    public CitizenReportMetricsAdapter(AvisRepository avisRepository, Clock clock) {
        this.avisRepository = avisRepository;
        this.clock = clock;
    }

    @Override
    public Map<String, Long> countByStatus() {
        Map<String, Long> counts = avisRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStatut() == null ? AvisStatus.SIGNALE.name() : a.getStatut().name(),
                        TreeMap::new, Collectors.counting()));
        // Les etats absents sont rendus a zero : un tableau ou « TRAITE » disparait quand rien
        // n'a ete traite se lit comme une donnee manquante, pas comme un resultat.
        for (AvisStatus status : AvisStatus.values()) {
            counts.putIfAbsent(status.name(), 0L);
        }
        return counts;
    }

    @Override
    public Optional<Duration> medianResolutionTime() {
        List<Duration> delays = avisRepository.findAll().stream()
                .filter(a -> a.getProcessedAt() != null && a.getSubmittedAt() != null)
                .map(a -> Duration.between(a.getSubmittedAt(), a.getProcessedAt()))
                .filter(d -> !d.isNegative())
                .sorted()
                .toList();
        if (delays.isEmpty()) {
            return Optional.empty();
        }
        int middle = delays.size() / 2;
        return Optional.of(delays.size() % 2 == 1
                ? delays.get(middle)
                // Nombre pair : moyenne des deux valeurs centrales, convention usuelle.
                : delays.get(middle - 1).plus(delays.get(middle)).dividedBy(2));
    }

    @Override
    public long countOpenOlderThan(Duration age) {
        Instant cutoff = Instant.now(clock).minus(age);
        return avisRepository.findAll().stream()
                .filter(a -> a.getStatut() == null || !a.getStatut().isTerminal())
                .filter(a -> a.getSubmittedAt() != null && a.getSubmittedAt().isBefore(cutoff))
                .count();
    }

}
