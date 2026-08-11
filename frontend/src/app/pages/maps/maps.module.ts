import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MapsRoutingModule } from './maps-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { MapsComponent } from './main-content/maps.component';
import { EsriComponent } from './esri/esri.component';


@NgModule({
  declarations: [
    EsriComponent
  ],
  imports: [
    SharedModule,
    MapsRoutingModule,
    MapsComponent,
  ]
})
export class MapsModule { }
