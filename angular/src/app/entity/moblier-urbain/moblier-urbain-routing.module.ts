import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/**
 * Routes de la ressource « moblier-urbains » : liste, création et édition sont rendues par les
 * composants CRUD génériques, configurés par `ENTITY_CONFIGS` (clé « moblier-urbains »).
 */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('moblier-urbains'))],
  exports: [RouterModule]
})
export class MoblierUrbainRoutingModule { }
