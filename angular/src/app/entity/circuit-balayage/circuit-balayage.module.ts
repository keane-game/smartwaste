import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { CircuitBalayageRoutingModule } from './circuit-balayage-routing.module';
import { ListCircuitBalayageComponent } from './list/list-circuit-balayage.component';

@NgModule({
  declarations: [
    ListCircuitBalayageComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    CircuitBalayageRoutingModule
  ]
})
export class CircuitBalayageModule { }
