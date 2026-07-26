import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AlertRoutingModule } from './alert-routing.module';
import { ListAlertComponent } from './list/list-alert.component';

@NgModule({
  declarations: [
    ListAlertComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    AlertRoutingModule
  ]
})
export class AlertModule { }
