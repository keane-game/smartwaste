package sn.smartwaste.collect.analytics.presentation.controller;

import sn.smartwaste.collect.analytics.application.dto.DepartmentState;
import sn.smartwaste.collect.analytics.application.service.DashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/data")
@RequiredArgsConstructor
/**
 * Indicateurs du tableau de bord (`GET /data/**`, public en lecture).
 *
 * <p>Les sept {@code POST /data/{ressource}} d'import de GeoJSON qui vivaient ici ont rejoint
 * {@code administration} ({@code DataImportController}) : ce sont des opérations d'exploitation,
 * pas de la supervision. Elles héritaient au passage du préfixe public de ce contrôleur — c'est
 * ainsi que des écritures en base étaient devenues joignables sans authentification.
 * Les URL sont <b>inchangées</b>.
 */
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping(value = "/departmentState" , produces = "application/json")
    public DepartmentState departmentState() {
        return dashboardService.departmentState ();
    }
}

