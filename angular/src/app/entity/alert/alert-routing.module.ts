import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListAlertComponent } from './list/list-alert.component';

const routes: Routes = [
  { path: '', component: ListAlertComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AlertRoutingModule { }
