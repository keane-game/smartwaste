import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListCircuitCollectComponent } from './list/list-circuit-collect.component';

const routes: Routes = [
  { path: '', component: ListCircuitCollectComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CircuitCollectRoutingModule { }
