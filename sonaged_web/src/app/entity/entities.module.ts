import { NgModule } from '@angular/core';
import { EntitiesRoutingModule } from './entities-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialsModule } from '../materials/material.module';

import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [
    //depotoir
  ],
 
  imports: [
    CommonModule,
    EntitiesRoutingModule,
  ],
})

export class EntitiesModule { }
