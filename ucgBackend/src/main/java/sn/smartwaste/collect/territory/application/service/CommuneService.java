package sn.smartwaste.collect.territory.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.territory.application.dto.Commune;

import java.util.List;

public interface CommuneService {
    Commune readCommune(UUID communeId);

    List<Commune> readAllCommune();

    Commune createCommune(Commune commune);

    Commune updateCommune(UUID communeId, Commune commune);

    void deleteCommune(UUID communeId);

    Page<Commune> readAllCommune(Pageable pageable);
}
