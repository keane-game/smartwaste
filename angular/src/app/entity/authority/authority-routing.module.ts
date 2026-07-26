import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { crudRoutes } from '../../shares/crud/crud.module';

/** Routes de la ressource « authorities » (CRUD générique). */
@NgModule({
  imports: [RouterModule.forChild(crudRoutes('authorities'))],
  exports: [RouterModule]
})
export class AuthorityRoutingModule { }
