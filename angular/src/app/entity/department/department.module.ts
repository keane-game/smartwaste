import { NgModule } from '@angular/core';
import { CrudModule } from '../../shares/crud/crud.module';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { DepartmentRoutingModule } from './department-routing.module';
import { ListDepartmentComponent } from './list/list-department.component';

@NgModule({
  declarations: [
    ListDepartmentComponent
  ],
  imports: [
    CrudModule,
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    DepartmentRoutingModule
  ]
})
export class DepartmentModule { }
