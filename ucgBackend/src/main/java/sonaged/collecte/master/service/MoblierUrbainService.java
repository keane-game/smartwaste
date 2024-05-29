package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.MoblierUrbain;

import java.util.List;

public interface MoblierUrbainService {
    MoblierUrbain getOneMoblierUrbain(Long moblierUrbainId);

    List<MoblierUrbain> getAllMoblierUrbain();

    MoblierUrbain createOneMoblierUrbain(MoblierUrbain moblierUrbain);

    MoblierUrbain updateOneMoblierUrbain(Long MoblierUrbainId, MoblierUrbain moblierUrbain);
    void deleteOneMoblierUrbain(Long moblierUrbainId);
}
