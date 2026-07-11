import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DepartmentComponent } from './department.component';
import { CreateDepartmentComponent } from './create-department/create-department.component';

const routes: Routes = [
  { path: '', component: DepartmentComponent },
  { path: '', component: CreateDepartmentComponent }
      ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DepartmentRoutingModule { }
