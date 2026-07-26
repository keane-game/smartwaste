import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListDepotoirComponent } from './list/list-depotoir.component';
import { CreateDepotoirComponent } from './create/create-depotoir.component';

const routes: Routes = [
  { path: '', component: ListDepotoirComponent },
  { path: 'create', component: CreateDepotoirComponent }
  ];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DepotoirRoutingModule { }
