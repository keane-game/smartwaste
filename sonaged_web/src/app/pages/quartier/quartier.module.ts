import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { QuartierRoutingModule } from './quartier-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MaterialsModule } from '../../materials/material.module';
import { QuartierComponent } from './quartier.component';
import { CreateQuartierComponent } from './create-quartier/create-quartier.component';


@NgModule({
  declarations: [
    QuartierComponent,
    CreateQuartierComponent
  ],
  imports: [
    CommonModule,
    QuartierRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    MaterialsModule,
  ]
})
export class QuartierModule { }
