package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.DepartmentState;

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
