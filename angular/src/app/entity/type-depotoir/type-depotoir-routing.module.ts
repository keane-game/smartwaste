import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListTypeDepotoirComponent } from './list/list-type-depotoir.component';

const routes: Routes = [
  { path: '', component: ListTypeDepotoirComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TypeDepotoirRoutingModule { }
