import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { AsyncPipe, CommonModule, NgComponentOutlet } from '@angular/common';

import { DepartmentRoutingModule } from './department-routing.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MaterialsModule } from '../../shared/materials/material.module';
import { DepartmentComponent } from './department.component';
import { CreateDepartmentComponent } from './create-department/create-department.component';


@NgModule({ declarations: [
        DepartmentComponent,
        CreateDepartmentComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA], imports: [CommonModule,
        DepartmentRoutingModule,
        ReactiveFormsModule,
        FormsModule,
        MaterialsModule,
        NgComponentOutlet, AsyncPipe], providers: [provideHttpClient(withInterceptorsFromDi())] })
export class DepartmentModule { }
