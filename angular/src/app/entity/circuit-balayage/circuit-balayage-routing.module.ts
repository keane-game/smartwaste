import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListCircuitBalayageComponent } from './list/list-circuit-balayage.component';

const routes: Routes = [
  { path: '', component: ListCircuitBalayageComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CircuitBalayageRoutingModule { }
