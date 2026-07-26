import { NgModule } from '@angular/core';
import { CrudModule } from '../../shares/crud/crud.module';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { TypeDepotoirRoutingModule } from './type-depotoir-routing.module';
import { ListTypeDepotoirComponent } from './list/list-type-depotoir.component';

@NgModule({
  declarations: [
    ListTypeDepotoirComponent
  ],
  imports: [
    CrudModule,
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    TypeDepotoirRoutingModule
  ]
})
export class TypeDepotoirModule { }
