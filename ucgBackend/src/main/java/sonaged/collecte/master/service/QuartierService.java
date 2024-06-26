package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Quartier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuartierService{
    Quartier readQuartier(Long quartierId);

    List<Quartier> readAllQuartier();

    Quartier createQuartier(Quartier quartier);

    Quartier updateQuartier(Long QuartierId, Quartier quartier);

    void deleteQuartier(Long quartierId);

    Page<Quartier> readAllQuartier(Pageable pageable);
}
