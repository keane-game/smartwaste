import { NgModule } from '@angular/core';

import { VehicleRoutingModule } from './vehicle-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { VehicleComponent } from './main-content/vehicle.component';

@NgModule({
  declarations: [
    VehicleComponent,
  ],
  imports: [
    SharedModule,
    VehicleRoutingModule
  ]
})
export class VehicleModule { }
