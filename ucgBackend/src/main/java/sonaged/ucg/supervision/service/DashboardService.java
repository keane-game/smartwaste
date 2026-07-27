package sonaged.ucg.supervision.service;

import sonaged.ucg.supervision.dto.DepartmentState;


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
