import { NgModule } from '@angular/core';

import { AlertRoutingModule } from './alert-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { AlertComponent } from './main-content/alert.component';
import { CreateAlertComponent } from './create-alert/create-alert.component';


@NgModule({
  declarations: [
    AlertComponent,
    CreateAlertComponent
  ],
  imports: [
    SharedModule,
    AlertRoutingModule
  ]
})
export class AlertModule { }
