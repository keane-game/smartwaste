import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListQuartierComponent } from './list/list-quartier.component';

const routes: Routes = [
  { path: '', component: ListQuartierComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class QuartierRoutingModule { }
