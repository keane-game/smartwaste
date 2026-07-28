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

    private final DepartmentService departmentService;

    public TerritoryReadModelAdapter(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Override
    public DepartmentMaps firstDepartmentForMap() {
        return departmentService.getFirstDepartment();
    }
}
