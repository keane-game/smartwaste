package sn.smartwaste.collect.waste.application.service;

import sn.smartwaste.collect.waste.application.dto.MoblierUrbain;

import java.util.List;

public interface MoblierUrbainService {
    MoblierUrbain readMoblierUrbain(Long moblierUrbainId);

    List<MoblierUrbain> readAllMoblierUrbain();

    MoblierUrbain createMoblierUrbain(MoblierUrbain moblierUrbain);

    MoblierUrbain updateMoblierUrbain(Long MoblierUrbainId, MoblierUrbain moblierUrbain);

    void deleteMoblierUrbain(Long moblierUrbainId);
}
