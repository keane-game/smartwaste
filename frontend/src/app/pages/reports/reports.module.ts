import { NgModule } from '@angular/core';

import { ReportsRoutingModule } from './reports-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { ReportsComponent } from './main-content/reports.component';

@NgModule({
  declarations: [
    ReportsComponent,
  ],
  imports: [
    SharedModule,
    ReportsRoutingModule
  ]
})
export class ReportsModule { }
