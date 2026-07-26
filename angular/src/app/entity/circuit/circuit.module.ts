import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { CircuitRoutingModule } from './circuit-routing.module';
import { ListCircuitComponent } from './list/list-circuit.component';

@NgModule({
  declarations: [
    ListCircuitComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    CircuitRoutingModule
  ]
})
export class CircuitModule { }
