import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RegionRoutingModule } from './region-routing.module';


@NgModule({
  declarations: [
    
  ],
  imports: [
    CommonModule,
    RegionRoutingModule,
  ],
  schemas:[CUSTOM_ELEMENTS_SCHEMA],
})
export class RegionModule { }
