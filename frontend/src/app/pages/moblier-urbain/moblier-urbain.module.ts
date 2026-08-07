import { NgModule } from '@angular/core';

import { MoblierUrbainRoutingModule } from './moblier-urbain-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { MoblierUrbainComponent } from './main-content/moblier-urbain.component';

@NgModule({
  declarations: [
    MoblierUrbainComponent,
  ],
  imports: [
    SharedModule,
    MoblierUrbainRoutingModule
  ]
})
export class MoblierUrbainModule { }
