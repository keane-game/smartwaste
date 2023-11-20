package ucg.collecte.master.service;

import ucg.collecte.master.dto.MobilieUrbainDto;

import java.util.List;

public interface MobilieUrbainService {
    MobilieUrbainDto getOneMobilieUrbain(Long mobilieUrbainId);

    List<MobilieUrbainDto> getAllMobilieUrbain();

    MobilieUrbainDto createOneMobilieUrbain(MobilieUrbainDto mobilieUrbainDto);

    MobilieUrbainDto updateOneMobilieUrbain(Long MobilieUrbainId, MobilieUrbainDto mobilieUrbainDto);
    void deleteOneMobilieUrbain(Long mobilieUrbainId);
}
