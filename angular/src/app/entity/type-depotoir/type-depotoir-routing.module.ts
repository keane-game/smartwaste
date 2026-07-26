import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/**
 * Routes de la ressource « typedepotoirs » : liste, création et édition sont rendues par les
 * composants CRUD génériques, configurés par `ENTITY_CONFIGS` (clé « typedepotoirs »).
 */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('typedepotoirs'))],
  exports: [RouterModule]
})
export class TypeDepotoirRoutingModule { }
