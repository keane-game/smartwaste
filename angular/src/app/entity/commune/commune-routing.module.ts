import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/**
 * Routes de la ressource « communes » : liste, création et édition sont rendues par les
 * composants CRUD génériques, configurés par `ENTITY_CONFIGS` (clé « communes »).
 */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('communes'))],
  exports: [RouterModule]
})
export class CommuneRoutingModule { }
