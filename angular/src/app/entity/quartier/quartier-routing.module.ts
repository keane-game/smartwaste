import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/**
 * Routes de la ressource « quartiers » : liste, création et édition sont rendues par les
 * composants CRUD génériques, configurés par `ENTITY_CONFIGS` (clé « quartiers »).
 */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('quartiers'))],
  exports: [RouterModule]
})
export class QuartierRoutingModule { }
