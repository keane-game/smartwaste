import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsersRoutingModule } from './users-routing.module';
import { CreateUserComponent } from './create/create-user.component';
import { ListUserComponent } from './list/list-user.component';
import { UpdateUserComponent } from './update/update-user.component';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialsModule } from '../../materials/material.module';

@NgModule({
 
  imports: [
    CommonModule,
    UsersRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    MaterialsModule,
  ],
  declarations: [
    CreateUserComponent,
    UpdateUserComponent,
    CreateUserComponent,
    ListUserComponent,
  ],

})
export class UserModule { }
