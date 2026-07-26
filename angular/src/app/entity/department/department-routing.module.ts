import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/**
 * Routes de la ressource « departments » : liste, création et édition sont rendues par les
 * composants CRUD génériques, configurés par `ENTITY_CONFIGS` (clé « departments »).
 */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('departments'))],
  exports: [RouterModule]
})
export class DepartmentRoutingModule { }
