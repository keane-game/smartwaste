import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { CommuneRoutingModule } from './commune-routing.module';
import { ListCommuneComponent } from './list/list-commune.component';

@NgModule({
  declarations: [
    ListCommuneComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    CommuneRoutingModule
  ]
})
export class CommuneModule { }
