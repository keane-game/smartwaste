import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CircuitBalayageComponent } from './main-content/circuit-balayage.component';

const routes: Routes = [{ path: '', component: CircuitBalayageComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CircuitBalayageRoutingModule { }
