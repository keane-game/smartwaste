package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.QuartierDto;

import java.util.List;

public interface QuartierService{
    QuartierDto getOneQuartier(Long quartierId);

    List<QuartierDto> getAllQuartier();

    QuartierDto createOneQuartier(QuartierDto quartierDto);

    QuartierDto updateOneQuartier(Long QuartierId, QuartierDto quartierDto);
    void deleteOneQuartier(Long quartierId);

}
