import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CircuitCollectComponent } from './main-content/circuit-collect.component';

const routes: Routes = [{ path: '', component: CircuitCollectComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CircuitCollectRoutingModule { }
