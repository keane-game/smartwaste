import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListMoblierUrbainComponent } from './list/list-moblier-urbain.component';

const routes: Routes = [
  { path: '', component: ListMoblierUrbainComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MoblierUrbainRoutingModule { }
