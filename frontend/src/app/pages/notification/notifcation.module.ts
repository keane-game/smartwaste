import {  CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotifcationRoutingModule } from './notifcation-routing.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MaterialsModule } from '../../shared/materials/material.module';
import { NotifcationComponent } from './notifcation.component';
import { CreateNotificationComponent } from './create-notification/create-notification.component';


@NgModule({ declarations: [
        NotifcationComponent,
        CreateNotificationComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA], imports: [CommonModule,
        NotifcationRoutingModule,
        ReactiveFormsModule,
        FormsModule,
        MaterialsModule], providers: [provideHttpClient(withInterceptorsFromDi())] })
export class NotifcationModule { }
