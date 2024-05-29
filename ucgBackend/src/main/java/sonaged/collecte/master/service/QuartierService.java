package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Quartier;

import java.util.List;

public interface QuartierService{
    Quartier getOneQuartier(Long quartierId);

    List<Quartier> getAllQuartier();

    Quartier createOneQuartier(Quartier quartier);

    Quartier updateOneQuartier(Long QuartierId, Quartier quartier);
    void deleteOneQuartier(Long quartierId);

}
