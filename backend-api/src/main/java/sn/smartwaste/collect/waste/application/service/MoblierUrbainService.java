package sn.smartwaste.collect.waste.application.service;

import sn.smartwaste.collect.waste.application.dto.MoblierUrbain;

import java.util.List;
import java.util.UUID;

public interface MoblierUrbainService {
    MoblierUrbain readMoblierUrbain(UUID moblierUrbainId);

    List<MoblierUrbain> readAllMoblierUrbain();

    MoblierUrbain createMoblierUrbain(MoblierUrbain moblierUrbain);

    MoblierUrbain updateMoblierUrbain(UUID moblierUrbainId, MoblierUrbain moblierUrbain);

    void deleteMoblierUrbain(UUID moblierUrbainId);
}
