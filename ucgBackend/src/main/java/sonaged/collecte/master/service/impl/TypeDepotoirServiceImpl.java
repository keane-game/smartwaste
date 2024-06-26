package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.dto.TypeDepotoir;
import sonaged.collecte.master.mapper.TypeDepotoirMapper;
import sonaged.collecte.master.repository.TypeDepotoirRepository;
import sonaged.collecte.master.service.TypeDepotoirService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class TypeDepotoirServiceImpl implements TypeDepotoirService {

    private final TypeDepotoirRepository typeDepotoirRepository;


    @Override
    public TypeDepotoir readTypeDepotoir(Long typeDepotoirId) {
        var typeDepotoir  = typeDepotoirRepository.findById(typeDepotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TypeDepotoir with id [%s] not found ".formatted(typeDepotoirId)
                ));

        return TypeDepotoirMapper.TDMP.asDto(typeDepotoir);
    }


    @Override
    public List<TypeDepotoir> readAllTypeDepotoir() {
        var typeDepotoirList = typeDepotoirRepository.findAll();
        return TypeDepotoirMapper.TDMP.asListDto(typeDepotoirList);
    }


    @Override
    public TypeDepotoir createTypeDepotoir(TypeDepotoir typeDepotoir) {
        var savedTypeDepotoir = typeDepotoirRepository.save(TypeDepotoirMapper.TDMP.asModel(typeDepotoir));
        return TypeDepotoirMapper.TDMP.asDto (savedTypeDepotoir);
    }


    @Override
    public TypeDepotoir updateTypeDepotoir(Long typeDepotoirId, TypeDepotoir typeDepotoirDto) {
        var existedtypeDepotoir = typeDepotoirRepository.findById(typeDepotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de depotoir with id [%s] not found to update ".formatted(typeDepotoirId)
                ));
        if (!Objects.equals(existedtypeDepotoir.getTypeDepotoirId(), typeDepotoirDto.getTypeDepotoirId ())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        existedtypeDepotoir.setName(typeDepotoirDto.getName());
        return TypeDepotoirMapper
                .TDMP.asDto(
                    typeDepotoirRepository.save(existedtypeDepotoir));
    }


    @Override
    public void deleteTypeDepotoir(Long typeDepotoirId) {
        var existedtypeDepotoir = typeDepotoirRepository.findById(typeDepotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de depotoir with id [%s] not found to update ".formatted(typeDepotoirId)
                ));
         typeDepotoirRepository.delete(existedtypeDepotoir);

    }
}
