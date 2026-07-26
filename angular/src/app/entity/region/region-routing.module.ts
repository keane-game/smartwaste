import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListRegionComponent } from './list/list-region.component';

const routes: Routes = [
  { path: '', component: ListRegionComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RegionRoutingModule { }
