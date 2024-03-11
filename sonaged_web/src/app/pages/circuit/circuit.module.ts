import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CircuitRoutingModule } from './circuit-routing.module';
import { CircuitComponent } from './circuit.component';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MaterialsModule } from '../../materials/material.module';
import { CreateCircuitComponent } from './create-circuit/create-circuit.component';


@NgModule({
  declarations: [
    CircuitComponent,
    CreateCircuitComponent
  ],
  imports: [
    CommonModule,
    CircuitRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    MaterialsModule,
  ]
})
export class CircuitModule { }
