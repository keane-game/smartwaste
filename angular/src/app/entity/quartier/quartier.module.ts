import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { QuartierRoutingModule } from './quartier-routing.module';
import { ListQuartierComponent } from './list/list-quartier.component';

@NgModule({
  declarations: [
    ListQuartierComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    QuartierRoutingModule
  ]
})
export class QuartierModule { }
