package ucg.collecte.master.service;

import ucg.collecte.master.dto.QuartierDto;

import java.util.List;

public interface QuartierService{
    QuartierDto getOneQuartier(Long quartierId);

    List<QuartierDto> getAllQuartier();

    QuartierDto createOneQuartier(QuartierDto quartierDto);

    QuartierDto updateOneQuartier(Long QuartierId, QuartierDto quartierDto);
    void deleteOneQuartier(Long quartierId);

}
