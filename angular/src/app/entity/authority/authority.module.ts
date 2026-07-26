import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AuthorityRoutingModule } from './authority-routing.module';
import { ListAuthorityComponent } from './list/list-authority.component';

@NgModule({
  declarations: [
    ListAuthorityComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    AuthorityRoutingModule
  ]
})
export class AuthorityModule { }
