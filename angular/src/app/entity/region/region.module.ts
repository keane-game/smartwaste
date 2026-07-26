import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { RegionRoutingModule } from './region-routing.module';
import { ListRegionComponent } from './list/list-region.component';

@NgModule({
  declarations: [
    ListRegionComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    RegionRoutingModule
  ]
})
export class RegionModule { }
