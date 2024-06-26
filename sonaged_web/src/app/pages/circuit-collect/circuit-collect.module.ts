import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../shared/shared.module';
import { CircuitCollectComponent } from './main-content/circuit-collect.component';
import { CreateCircuitCollectComponent } from './create-circuit-collect/create-circuit-collect.component';
import { DeleteCircuitCollectComponent } from './delete-circuit-collect/delete-circuit-collect.component';
import { CircuitCollectRoutingModule } from './circuit-collect-routing.module';



@NgModule({
  declarations: [
    CircuitCollectComponent,
    CreateCircuitCollectComponent,
    DeleteCircuitCollectComponent
  ],
  imports: [
    SharedModule,
    CircuitCollectRoutingModule
  ]
})
export class CircuitCollectModule { }
