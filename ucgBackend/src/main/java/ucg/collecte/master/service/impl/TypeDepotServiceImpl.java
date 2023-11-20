package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucg.collecte.master.dto.TypeDepotDto;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.TypeDepotMapper;
import ucg.collecte.master.model.TypeDepot;
import ucg.collecte.master.repository.TypeDepotRepository;
import ucg.collecte.master.service.TypeDepotService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class TypeDepotServiceImpl implements TypeDepotService {

    private final TypeDepotRepository typeDepotRepository;
    /**
     * @param typeDepotId 
     * @return
     */
    @Override
    public TypeDepotDto getOneTypeDepot(Long typeDepotId) {
        TypeDepot typeDepot  = typeDepotRepository.findById(typeDepotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TypeDepot with id [%s] not found ".formatted(typeDepotId)
                ));

        return TypeDepotMapper.TDMP.modelToDto(typeDepot);
    }

    /**
     * @return 
     */
    @Override
    public List<TypeDepotDto> getAllTypeDepot() {
        List<TypeDepot> typeDepotList = typeDepotRepository.findAll();
        return TypeDepotMapper.TDMP.listModelToDto(typeDepotList);
    }

    /**
     * @param typeDepotDto
     * @return
     */
    @Override
    public TypeDepotDto createOneTypeDepot(TypeDepotDto typeDepotDto) {
        TypeDepot typeDepot = TypeDepot.builder()
                .typeDepotName(typeDepotDto.getTypeDepotName())
                .build();
        return TypeDepotMapper.TDMP.modelToDto(typeDepotRepository.save(typeDepot));
    }

    /**
     * @param typeDepotId 
     * @param typeDepotDto
     * @return
     */
    @Override
    public TypeDepotDto updateOneTypeDepot(Long typeDepotId, TypeDepotDto typeDepotDto) {
        TypeDepot existedtypeDepot = typeDepotRepository.findById(typeDepotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de depot with id [%s] not found to update ".formatted(typeDepotId)
                ));
        if (!Objects.equals(existedtypeDepot.getTypeDepotId(), typeDepotDto.getTypeDepotId())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        existedtypeDepot.setTypeDepotName(typeDepotDto.getTypeDepotName());
        return TypeDepotMapper
                .TDMP.modelToDto(
                    typeDepotRepository.save(existedtypeDepot));
    }

    /**
     * @param typeDepotId 
     */
    @Override
    public void deleteOneTypeDepot(Long typeDepotId) {
        TypeDepot existedtypeDepot = typeDepotRepository.findById(typeDepotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de depot with id [%s] not found to update ".formatted(typeDepotId)
                ));
         typeDepotRepository.delete(existedtypeDepot);

    }
}
