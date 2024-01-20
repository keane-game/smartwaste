package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.TypeDepotoirDto;
import sonaged.collecte.master.model.TypeDepotoir;

import java.util.List;

public interface TypeDepotoirService {
    TypeDepotoirDto getOneTypeDepotoir(Long typeDepotoirId);

    List<TypeDepotoirDto> getAllTypeDepotoir();

    TypeDepotoirDto createOneTypeDepotoir(TypeDepotoirDto typeDepotoir);

    TypeDepotoirDto updateOneTypeDepotoir(Long typeDepotoirId, TypeDepotoirDto typeDepotoir);


    void deleteOneTypeDepotoir(Long typeDepotoirId);
}
