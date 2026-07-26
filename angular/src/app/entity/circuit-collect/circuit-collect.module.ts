import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { CircuitCollectRoutingModule } from './circuit-collect-routing.module';
import { ListCircuitCollectComponent } from './list/list-circuit-collect.component';

@NgModule({
  declarations: [
    ListCircuitCollectComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    CircuitCollectRoutingModule
  ]
})
export class CircuitCollectModule { }
