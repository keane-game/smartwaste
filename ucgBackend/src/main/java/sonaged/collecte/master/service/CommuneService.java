package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.CommuneDto;

import java.util.List;

public interface CommuneService {
    CommuneDto getOneCommune(Long communeId);

    List<CommuneDto> getAllCommune();

    CommuneDto createOneCommune(CommuneDto communeDto);

    CommuneDto updateOneCommune(Long communeId, CommuneDto communeDto);
    void deleteOneCommune(Long communeId);
}
