package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.MoblierUrbainDto;

import java.util.List;

public interface MoblierUrbainService {
    MoblierUrbainDto getOneMoblierUrbain(Long moblierUrbainId);

    List<MoblierUrbainDto> getAllMoblierUrbain();

    MoblierUrbainDto createOneMoblierUrbain(MoblierUrbainDto moblierUrbainDto);

    MoblierUrbainDto updateOneMoblierUrbain(Long MoblierUrbainId, MoblierUrbainDto moblierUrbainDto);
    void deleteOneMoblierUrbain(Long moblierUrbainId);
}
