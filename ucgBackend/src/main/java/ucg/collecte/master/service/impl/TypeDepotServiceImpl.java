package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.UserMapper;
import ucg.collecte.master.model.TypeDepot;
import ucg.collecte.master.model.User;
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
    public TypeDepot getOneTypeDepot(Long typeDepotId) {
        TypeDepot typeDepot  = typeDepotRepository.findById(typeDepotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TypeDepot with id [%s] not found ".formatted(typeDepotId)
                ));
        return typeDepot;
    }

    /**
     * @return 
     */
    @Override
    public List<TypeDepot> getAllTypeDepot() {
        List<TypeDepot> typeDepotList = typeDepotRepository.findAll();
        return typeDepotList.stream().toList();
    }

    /**
     * @param typeDepot 
     * @return
     */
    @Override
    public TypeDepot createOneTypeDepot(TypeDepot typeDepot) {
        return typeDepotRepository.save(typeDepot);
    }

    /**
     * @param typeDepotId 
     * @param typeDepot
     * @return
     */
    @Override
    public TypeDepot updateOneTypeDepot(Long typeDepotId, TypeDepot typeDepot) {
        TypeDepot existedtypeDepot = typeDepotRepository.findById(typeDepotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de depot with id [%s] not found to update ".formatted(typeDepotId)
                ));
        if (!Objects.equals(existedtypeDepot.getTypeDepotId(), typeDepot.getTypeDepotId())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        existedtypeDepot.setTypeDepotName(typeDepot.getTypeDepotName());
        return typeDepotRepository.save(existedtypeDepot);
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
