import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/**
 * Routes de la ressource « circuit-collects » : liste, création et édition sont rendues par les
 * composants CRUD génériques, configurés par `ENTITY_CONFIGS` (clé « circuit-collects »).
 */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('circuit-collects'))],
  exports: [RouterModule]
})
export class CircuitCollectRoutingModule { }
