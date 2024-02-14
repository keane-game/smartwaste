import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DepotoirRoutingModule } from './depotoir-routing.module';
import { DepotoirComponent } from './depotoir.component';
import { CreateDepotoirComponent } from './create/create-depotoir.component';
import { ListDepotoirComponent } from './list/list-depotoir.component';


@NgModule({
  declarations: [
    DepotoirComponent,
    CreateDepotoirComponent,
    ListDepotoirComponent
  ],
  imports: [
    CommonModule,
    DepotoirRoutingModule
  ]
})
export class DepotoirModule { }
