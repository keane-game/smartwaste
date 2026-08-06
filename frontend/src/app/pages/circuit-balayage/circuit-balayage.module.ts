import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CircuitBalayageRoutingModule } from './circuit-balayage-routing.module';
import { CircuitBalayageComponent } from './main-content/circuit-balayage.component';
import { CreateCircuitBalayageComponent } from './create-circuit-balayage/create-circuit-balayage.component';
import { DeleteCircuitBalayageComponent } from './delete-circuit-balayage/delete-circuit-balayage.component';
import { SharedModule } from '../../shared/shared.module';


@NgModule({
  declarations: [
    CircuitBalayageComponent,
    CreateCircuitBalayageComponent,
    DeleteCircuitBalayageComponent
  ],
  imports: [
    SharedModule,
    CircuitBalayageRoutingModule
  ]
})
export class CircuitBalayageModule { }
