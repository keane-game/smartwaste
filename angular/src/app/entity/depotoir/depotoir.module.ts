import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

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
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    DepotoirRoutingModule
  ]
})
export class DepotoirModule { }
