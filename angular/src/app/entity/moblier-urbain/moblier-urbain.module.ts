import { NgModule } from '@angular/core';
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
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    MoblierUrbainRoutingModule
  ]
})
export class MoblierUrbainModule { }
