import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/** Routes de la ressource « geometries » (CRUD générique). */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('geometries'))],
  exports: [RouterModule]
})
export class GeometryRoutingModule { }
