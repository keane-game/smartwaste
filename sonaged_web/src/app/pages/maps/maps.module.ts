import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MapsRoutingModule } from './maps-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { MapsComponent } from './main-content/maps.component';


@NgModule({
  declarations: [
    MapsComponent
  ],
  imports: [
    SharedModule,
    MapsRoutingModule
  ]
})
export class MapsModule { }
