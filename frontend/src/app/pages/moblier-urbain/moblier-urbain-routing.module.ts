import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MoblierUrbainComponent } from './main-content/moblier-urbain.component';

const routes: Routes = [
  { path: '', component: MoblierUrbainComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MoblierUrbainRoutingModule { }
