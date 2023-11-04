package ucg.collecte.master.service;

import ucg.collecte.master.dto.CommuneDto;

import java.util.List;

public interface CommuneService {
    CommuneDto getOneCommune(Long communeId);

    List<CommuneDto> getAllCommune();

    CommuneDto createOneCommune(CommuneDto communeDto);

    CommuneDto updateOneCommune(Long communeId, CommuneDto communeDto);
    void deleteOneCommune(Long communeId);
}
