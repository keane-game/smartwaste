import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { AsyncPipe, CommonModule, NgComponentOutlet } from '@angular/common';

import { DepartmentRoutingModule } from './department-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MaterialsModule } from '../../shared/materials/material.module';
import { DepartmentComponent } from './department.component';
import { CreateDepartmentComponent } from './create-department/create-department.component';


@NgModule({
  declarations: [
    DepartmentComponent,
    CreateDepartmentComponent
  ],
  imports: [
    CommonModule,
    DepartmentRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    MaterialsModule,
    NgComponentOutlet, AsyncPipe
  ], schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class DepartmentModule { }
