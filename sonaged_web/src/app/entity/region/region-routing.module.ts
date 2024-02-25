import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegionComponent } from './region.component';
import { CreateRegionComponent } from './create-region/create-region.component';

const routes: Routes = [
    { path: '', component: RegionComponent },
    { path: '', component: CreateRegionComponent }  
  ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RegionRoutingModule { }
