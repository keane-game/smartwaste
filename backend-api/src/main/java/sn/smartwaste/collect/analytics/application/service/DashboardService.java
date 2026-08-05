package sn.smartwaste.collect.analytics.application.service;

import sn.smartwaste.collect.analytics.application.dto.DepartmentState;


public interface DashboardService {

    Long totalCommunes();

    Long totalDepotoirs();

    Long totalBennes();

    Long totalCircuits();

    Long totalBacs();

    Long totalPRN();
    Long totalPP();
    Long totalCP();
    Long totalHabitans();

    DepartmentState departmentState();
}
