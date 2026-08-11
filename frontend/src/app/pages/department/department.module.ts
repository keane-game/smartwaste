import { NgModule } from '@angular/core';
import { AsyncPipe, CommonModule, NgComponentOutlet } from '@angular/common';

import { DepartmentRoutingModule } from './department-routing.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { DepartmentComponent } from './department.component';
import { CreateDepartmentComponent } from './create-department/create-department.component';


@NgModule({ declarations: [
        DepartmentComponent,
        CreateDepartmentComponent
    ],
    // `SharedModule` (comme Commune/Quartier) plutot que `MaterialsModule` seul + un
    // `CUSTOM_ELEMENTS_SCHEMA` de complaisance : importer les deux a la fois embrouillait la
    // portee de compilation Ivy — `app-pagination-custumer` (fourni par `SharedModule`) ne
    // resolvait plus son type d'`@Output` correctement, `$event` retombait sur `Event` generique
    // au lieu de `{pageIndex, pageSize}` (erreur TS2345 a la compilation, trouve en ajoutant la
    // pagination serveur ici pour matcher Commune/Quartier).
    imports: [CommonModule,
        DepartmentRoutingModule,
        ReactiveFormsModule,
        FormsModule,
        SharedModule,
        NgComponentOutlet, AsyncPipe], providers: [provideHttpClient(withInterceptorsFromDi())] })
export class DepartmentModule { }
