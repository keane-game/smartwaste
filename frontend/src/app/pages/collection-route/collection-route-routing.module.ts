import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CollectionRouteComponent } from './main-content/collection-route.component';

const routes: Routes = [
  { path: '', component: CollectionRouteComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CollectionRouteRoutingModule { }
