import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegionComponent } from './region.component';

// `CreateRegionComponent` n'est pas une route : c'est le dialogue ouvert par `RegionComponent`
// (ModalService), pas un écran de navigation. La seconde entrée sur le même chemin vide était
// de toute façon inatteignable (même piège que maps-routing.module.ts avant correctif).
const routes: Routes = [
    { path: '', component: RegionComponent },
  ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RegionRoutingModule { }
