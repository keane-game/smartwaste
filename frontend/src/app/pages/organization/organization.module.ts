import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { OrganizationRoutingModule } from './organization-routing.module';
import { OrganizationComponent } from './main-content/organization.component';

@NgModule({
  declarations: [
    OrganizationComponent,
  ],
  imports: [
    SharedModule,
    OrganizationRoutingModule
  ]
})
export class OrganizationModule { }
