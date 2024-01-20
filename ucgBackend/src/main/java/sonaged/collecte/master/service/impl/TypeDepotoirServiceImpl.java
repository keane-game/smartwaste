package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.dto.TypeDepotoirDto;
import sonaged.collecte.master.mapper.TypeDepotoirMapper;
import sonaged.collecte.master.model.TypeDepotoir;
import sonaged.collecte.master.repository.TypeDepotoirRepository;
import sonaged.collecte.master.service.TypeDepotoirService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class TypeDepotoirServiceImpl implements TypeDepotoirService {

    private final TypeDepotoirRepository typeDepotoirRepository;
    /**
     * @param typeDepotoirId
     * @return
     */
    @Override
    public TypeDepotoirDto getOneTypeDepotoir(Long typeDepotoirId) {
        TypeDepotoir typeDepotoir  = typeDepotoirRepository.findById(typeDepotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TypeDepotoir with id [%s] not found ".formatted(typeDepotoirId)
                ));

        return TypeDepotoirMapper.TDMP.modelToDto(typeDepotoir);
    }

    /**
     * @return 
     */
    @Override
    public List<TypeDepotoirDto> getAllTypeDepotoir() {
        List<TypeDepotoir> typeDepotoirList = typeDepotoirRepository.findAll();
        return TypeDepotoirMapper.TDMP.listModelToDto(typeDepotoirList);
    }

    /**
     * @param typeDepotoirDto
     * @return
     */
    @Override
    public TypeDepotoirDto createOneTypeDepotoir(TypeDepotoirDto typeDepotoirDto) {
        TypeDepotoir typeDepotoir = TypeDepotoir.builder()
                .typeDepotoirName(typeDepotoirDto.getTypeDepotoirName())
                .build();
        return TypeDepotoirMapper.TDMP.modelToDto(typeDepotoirRepository.save(typeDepotoir));
    }

    /**
     * @param typeDepotoirId
     * @param typeDepotoirDto
     * @return
     */
    @Override
    public TypeDepotoirDto updateOneTypeDepotoir(Long typeDepotoirId, TypeDepotoirDto typeDepotoirDto) {
        TypeDepotoir existedtypeDepotoir = typeDepotoirRepository.findById(typeDepotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de depotoir with id [%s] not found to update ".formatted(typeDepotoirId)
                ));
        if (!Objects.equals(existedtypeDepotoir.getTypeDepotoirId(), typeDepotoirDto.getTypeDepotoirId())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        existedtypeDepotoir.setTypeDepotoirName(typeDepotoirDto.getTypeDepotoirName());
        return TypeDepotoirMapper
                .TDMP.modelToDto(
                    typeDepotoirRepository.save(existedtypeDepotoir));
    }

    /**
     * @param typeDepotoirId
     */
    @Override
    public void deleteOneTypeDepotoir(Long typeDepotoirId) {
        TypeDepotoir existedtypeDepotoir = typeDepotoirRepository.findById(typeDepotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de depotoir with id [%s] not found to update ".formatted(typeDepotoirId)
                ));
         typeDepotoirRepository.delete(existedtypeDepotoir);

    }
}
