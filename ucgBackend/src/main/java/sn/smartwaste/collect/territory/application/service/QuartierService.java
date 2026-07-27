package sn.smartwaste.collect.territory.application.service;

import java.util.UUID;

import sn.smartwaste.collect.territory.application.dto.Quartier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuartierService{
    Quartier readQuartier(UUID quartierId);

    List<Quartier> readAllQuartier();

    Quartier createQuartier(Quartier quartier);

    Quartier updateQuartier(UUID QuartierId, Quartier quartier);

    void deleteQuartier(UUID quartierId);

    Page<Quartier> readAllQuartier(Pageable pageable);
}
