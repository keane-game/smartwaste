import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MapsComponent } from './main-content/maps.component';
import { EsriComponent } from './esri/esri.component';

const routes: Routes = [
  { path: '', component: MapsComponent },
  { path: '', component: EsriComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MapsRoutingModule { }
