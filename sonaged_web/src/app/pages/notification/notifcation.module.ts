import {  CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotifcationRoutingModule } from './notifcation-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MaterialsModule } from '../../materials/material.module';
import { NotifcationComponent } from './notifcation.component';
import { CreateNotificationComponent } from './create-notification/create-notification.component';


@NgModule({
  declarations: [
    NotifcationComponent,
    CreateNotificationComponent
  ],
  imports: [
    CommonModule,
    NotifcationRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    MaterialsModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class NotifcationModule { }
