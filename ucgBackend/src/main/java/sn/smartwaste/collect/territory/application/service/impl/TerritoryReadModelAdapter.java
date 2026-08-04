package sn.smartwaste.collect.territory.application.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.territory.application.api.DepartmentMaps;
import sn.smartwaste.collect.territory.application.api.TerritoryReadModel;
import sn.smartwaste.collect.territory.application.service.DepartmentService;

/**
 * Implémentation du contrat de lecture publié par le référentiel territorial.
 *
 * <p>Délègue au service applicatif interne : le rôle de cette classe est de fixer une frontière,
 * pas de dupliquer une règle. {@code @Transactional(readOnly = true)} garantit que la géométrie
 * (LAZY) est résolue ici, dans la transaction du contexte propriétaire.
 */
@Service
@Transactional(readOnly = true)
public class TerritoryReadModelAdapter implements TerritoryReadModel {

    private final sn.smartwaste.collect.territory.domain.repository.CommuneRepository communeRepository;

    private final DepartmentService departmentService;

    public TerritoryReadModelAdapter(DepartmentService departmentService,
            sn.smartwaste.collect.territory.domain.repository.CommuneRepository communeRepository) {
        this.communeRepository = communeRepository;
        this.departmentService = departmentService;
    }

    @Override
    public DepartmentMaps firstDepartmentForMap() {
        return departmentService.getFirstDepartment();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String communeNameOf(java.util.UUID communeId) {
        if (communeId == null) {
            return null;
        }
        return communeRepository.findById(communeId)
                .map(sn.smartwaste.collect.territory.domain.model.CommuneEntity::getName)
                .orElse(null);
    }
}
