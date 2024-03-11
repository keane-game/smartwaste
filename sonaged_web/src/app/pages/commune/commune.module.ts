import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CommuneRoutingModule } from './commune-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MaterialsModule } from '../../materials/material.module';
import { CreateCommuneComponent } from './create-commune/create-commune.component';
import { CommuneComponent } from './commune.component';


@NgModule({
  declarations: [
    CommuneComponent,
    CreateCommuneComponent
  ],
  imports: [
    CommonModule,
    CommuneRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    MaterialsModule,
  ]
})
export class CommuneModule { }
