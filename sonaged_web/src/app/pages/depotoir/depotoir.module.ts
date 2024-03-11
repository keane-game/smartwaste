import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DepotoirRoutingModule } from './depotoir-routing.module';
import { DepotoirComponent } from './depotoir.component';
import { CreateDepotoirComponent } from './create/create-depotoir.component';
import { MaterialsModule } from '../../materials/material.module';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    DepotoirComponent,
    CreateDepotoirComponent,
  ],
  imports: [
    CommonModule,
    DepotoirRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    MaterialsModule,
  ]
})
export class DepotoirModule { }
