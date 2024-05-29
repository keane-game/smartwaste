package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Commune;

import java.util.List;

public interface CommuneService {
    Commune getOneCommune(Long communeId);

    List<Commune> getAllCommune();

    Commune createOneCommune(Commune communeDto);

    Commune updateOneCommune(Long communeId, Commune commune);
    void deleteOneCommune(Long communeId);
}
