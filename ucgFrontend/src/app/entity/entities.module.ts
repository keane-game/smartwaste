import { NgModule } from '@angular/core';
import { EntitiesRoutingModule } from './entities-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialsModule } from '../materials/material.module';

import { CommonModule } from '@angular/common';
import { ListUserComponent } from './users/list-user/list-user.component';
import { DepotComponent } from './depot/depot.component';
import { UpdateUserComponent } from './users/update-user/update-user.component';
import { CreateUserComponent } from './users/create-user/create-user.component';

@NgModule({
  declarations: [
    DepotComponent,
    CreateUserComponent,
    UpdateUserComponent,
    CreateUserComponent,
    ListUserComponent
  ],
 
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    EntitiesRoutingModule,
    ReactiveFormsModule,
    MaterialsModule,
    
  ],
})

export class EntitiesModule { }
