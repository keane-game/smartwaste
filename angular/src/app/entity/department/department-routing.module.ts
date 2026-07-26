import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListDepartmentComponent } from './list/list-department.component';

const routes: Routes = [
  { path: '', component: ListDepartmentComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DepartmentRoutingModule { }
