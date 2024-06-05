package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Quartier;

import java.util.List;

public interface QuartierService{
    Quartier readQuartier(Long quartierId);

    List<Quartier> readAllQuartier();

    Quartier createQuartier(Quartier quartier);

    Quartier updateQuartier(Long QuartierId, Quartier quartier);

    void deleteQuartier(Long quartierId);

}
