import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/**
 * Routes de la ressource « circuits » : liste, création et édition sont rendues par les
 * composants CRUD génériques, configurés par `ENTITY_CONFIGS` (clé « circuits »).
 */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('circuits'))],
  exports: [RouterModule]
})
export class CircuitRoutingModule { }
