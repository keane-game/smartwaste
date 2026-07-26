import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { DataImportRoutingModule } from './data-import-routing.module';
import { DataImportComponent } from './data-import.component';

@NgModule({
  declarations: [
    DataImportComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    DataImportRoutingModule
  ]
})
export class DataImportModule { }
