package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.CommuneMapper;
import sonaged.collecte.master.mapper.RegionMapper;
import sonaged.collecte.master.repository.CommuneRepository;
import sonaged.collecte.master.repository.QuartierRepository;
import sonaged.collecte.master.dto.QuartierDto;
import sonaged.collecte.master.mapper.QuartierMapper;
import sonaged.collecte.master.model.Quartier;
import sonaged.collecte.master.service.QuartierService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class QuartierServiceImpl implements QuartierService {
    private final CommuneRepository communeRepository;

    private final QuartierRepository quartierRepository;
    /**
     * @param quartierId
     * @return
     */
    @Override
    public QuartierDto getOneQuartier(Long quartierId) {
        Quartier quartier = quartierRepository.findById(quartierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quartier with id [%s] not found ".formatted(quartierId)
                ));
        return QuartierMapper.QMP.modelToDto(quartier);
    }

    /**
     * @return
     */
    @Override
    public List<QuartierDto> getAllQuartier() {
        List <Quartier> quartierList = quartierRepository.findAll();
        return QuartierMapper.QMP.listModelToDto(quartierList);
    }

    /**
     * @param quartierDto
     * @return
     */
    @Override
    public QuartierDto createOneQuartier(QuartierDto quartierDto) {

        var communeId = quartierDto.getCommune().getCommuneId ();
        if(communeId != null) {
            var commune = communeRepository.findById (communeId).orElseThrow (
                    () -> new ResourceNotFoundException ("")
            );
            quartierDto.setCommune (CommuneMapper.COMP.modelToDto (commune));
        }
        var quartierSave = quartierRepository.save(QuartierMapper.QMP.dtoToModel (quartierDto));
        return QuartierMapper.QMP.modelToDto(quartierSave);
    }

    /**
     * @param quartierId
     * @param quartierDto
     * @return
     */
    @Override
    public QuartierDto updateOneQuartier(Long quartierId, QuartierDto quartierDto) {
        Quartier existedQuartier = quartierRepository.findById(quartierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quartier with id [%s] not found to update ".formatted(quartierId)
                ));
        if (!Objects.equals(existedQuartier.getQuartierId(), quartierDto.getQuartierId())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        if(quartierDto.getQuartierName() != null){
            existedQuartier.setQuartierName(quartierDto.getQuartierName());
        }
        if(quartierDto.getQuartierArea() != null){
            existedQuartier.setQuartierArea(quartierDto.getQuartierArea());
        }
        if(quartierDto.getQuartierCav() != null){
            existedQuartier.setQuartierCav(quartierDto.getQuartierCav());
        }
        if(quartierDto.getQuartierCcrca() != null){
            existedQuartier.setQuartierCcrca(quartierDto.getQuartierCcrca());
        }
        if(quartierDto.getQuartierLength() != null){
            existedQuartier.setQuartierLength(quartierDto.getQuartierLength());
        }
        if(quartierDto.getQuartierNumerozr() != null){
            existedQuartier.setQuartierNumerozr(quartierDto.getQuartierNumerozr());
        }
        if(quartierDto.getQuartierPoucentage() != null){
            existedQuartier.setQuartierPoucentage(quartierDto.getQuartierPoucentage());
        }
        if(quartierDto.getQuartierCodeCav() != null){
            existedQuartier.setQuartierCodeCav(quartierDto.getQuartierCodeCav());
        }
        if(quartierDto.getQuartierCodeCcrca() != null){
            existedQuartier.setQuartierCodeCcrca(quartierDto.getQuartierCodeCcrca());
        }
        if(quartierDto.getQuartierCodeEntity() != null){
            existedQuartier.setQuartierCodeEntity(quartierDto.getQuartierCodeEntity());
        }
        if(quartierDto.getQuartierCodeSzr() != null){
            existedQuartier.setQuartierCodeSzr(quartierDto.getQuartierCodeSzr());
        }
        if(quartierDto.getQuartierZoneCoron() != null){
            existedQuartier.setQuartierZoneCoron(quartierDto.getQuartierZoneCoron());
        }
        if(quartierDto.getQuartierCode() != null){
            existedQuartier.setQuartierCode(quartierDto.getQuartierCode());
        }
        if(quartierDto.getQuartierArea() != null){
            existedQuartier.setQuartierArea(quartierDto.getQuartierArea());
        }

        Quartier quartierUpdate = quartierRepository.save(existedQuartier);
        return QuartierMapper.QMP.modelToDto(quartierUpdate);
    }

    /**
     * @param quartierId
     */
    @Override
    public void deleteOneQuartier(Long quartierId) {
        Quartier quartier = quartierRepository.findById(quartierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quartier with id [%s] not found ".formatted(quartierId)
                ));
        quartierRepository.delete(quartier);
    }
}
