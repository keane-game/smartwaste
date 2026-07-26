import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/** Routes de la ressource « coordinates » (CRUD générique). */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('coordinates'))],
  exports: [RouterModule]
})
export class CoordinateRoutingModule { }
