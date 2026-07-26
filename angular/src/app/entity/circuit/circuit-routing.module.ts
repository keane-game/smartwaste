import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListCircuitComponent } from './list/list-circuit.component';

const routes: Routes = [
  { path: '', component: ListCircuitComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CircuitRoutingModule { }
