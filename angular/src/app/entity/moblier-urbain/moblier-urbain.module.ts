import { NgModule } from '@angular/core';
import { CrudModule } from '../../shares/crud/crud.module';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { MoblierUrbainRoutingModule } from './moblier-urbain-routing.module';
import { ListMoblierUrbainComponent } from './list/list-moblier-urbain.component';

@NgModule({
  declarations: [
    ListMoblierUrbainComponent
  ],
  imports: [
    CrudModule,
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    MoblierUrbainRoutingModule
  ]
})
export class MoblierUrbainModule { }
