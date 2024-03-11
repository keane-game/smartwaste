import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DepotoirComponent } from './depotoir.component';
import { CreateDepotoirComponent } from './create/create-depotoir.component';

const routes: Routes = [
  { path: '', component: DepotoirComponent },
  { path: '', component: CreateDepotoirComponent }

  ];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DepotoirRoutingModule { }
