import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { SignupComponent } from './signup.component';
import { SignupRoutingModule } from './signup-routing.module';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SignupRoutingModule
  ],
  exports: [
    SignupComponent
  ],
  declarations: [
    SignupComponent
  ],
  providers: [
  ],
})
export class SignupModule { }