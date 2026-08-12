package sn.smartwaste.collect.waste.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.waste.application.dto.MoblierUrbain;

import java.util.List;
import java.util.UUID;

public interface MoblierUrbainService {
    MoblierUrbain readMoblierUrbain(UUID moblierUrbainId);

    List<MoblierUrbain> readAllMoblierUrbain();

    /** Corrige un gap relevé par audit (2026-08-10) : ressource sans pagination. */
    Page<MoblierUrbain> readAllMoblierUrbain(Pageable pageable);

    MoblierUrbain createMoblierUrbain(MoblierUrbain moblierUrbain);

    MoblierUrbain updateMoblierUrbain(UUID moblierUrbainId, MoblierUrbain moblierUrbain);

    void deleteMoblierUrbain(UUID moblierUrbainId);
}
