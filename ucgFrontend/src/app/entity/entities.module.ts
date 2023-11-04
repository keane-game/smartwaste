import { NgModule } from '@angular/core';
import { EntitiesRoutingModule } from './entities-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';



@NgModule({
  declarations: [
  ],
  imports: [
    FormsModule,
    HttpClientModule,
    EntitiesRoutingModule
  ],
})

export class EntitiesModule { }
