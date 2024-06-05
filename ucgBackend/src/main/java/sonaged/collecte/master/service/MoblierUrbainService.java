package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.MoblierUrbain;

import java.util.List;

public interface MoblierUrbainService {
    MoblierUrbain readMoblierUrbain(Long moblierUrbainId);

    List<MoblierUrbain> readAllMoblierUrbain();

    MoblierUrbain createMoblierUrbain(MoblierUrbain moblierUrbain);

    MoblierUrbain updateMoblierUrbain(Long MoblierUrbainId, MoblierUrbain moblierUrbain);

    void deleteMoblierUrbain(Long moblierUrbainId);
}
