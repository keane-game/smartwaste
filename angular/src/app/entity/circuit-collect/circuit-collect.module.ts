import { NgModule } from '@angular/core';
import { CrudModule } from '../../shares/crud/crud.module';
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
    CrudModule,
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    CircuitCollectRoutingModule
  ]
})
export class CircuitCollectModule { }
