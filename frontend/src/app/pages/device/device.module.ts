import { NgModule } from '@angular/core';

import { DeviceRoutingModule } from './device-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { DeviceComponent } from './main-content/device.component';

@NgModule({
  declarations: [
    DeviceComponent,
  ],
  imports: [
    SharedModule,
    DeviceRoutingModule
  ]
})
export class DeviceModule { }
