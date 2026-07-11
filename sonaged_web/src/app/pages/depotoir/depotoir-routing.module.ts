import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DepotoirComponent } from './main-content/depotoir.component';
import { CreateDepotoirComponent } from './create-depotoir/create-depotoir.component';

const routes: Routes = [
  { path: '', component: DepotoirComponent },
  { path: '', component: CreateDepotoirComponent }

  ];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DepotoirRoutingModule { }
