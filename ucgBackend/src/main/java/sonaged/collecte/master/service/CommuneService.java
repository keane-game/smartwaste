package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Commune;

import java.util.List;

public interface CommuneService {
    Commune readCommune(Long communeId);

    List<Commune> readAllCommune();

    Commune createCommune(Commune commune);

    Commune updateCommune(Long communeId, Commune commune);

    void deleteCommune(Long communeId);
}
