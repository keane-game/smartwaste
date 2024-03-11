import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CircuitComponent } from './circuit.component';

const routes: Routes = [{ path: '', component: CircuitComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CircuitRoutingModule { }
