import { NgModule } from '@angular/core';

import { CommuneRoutingModule } from './commune-routing.module';
import { CreateCommuneComponent } from './create-commune/create-commune.component';
import { CommuneComponent } from './main-content/commune.component';
import { SharedModule } from '../../shared/shared.module';
import { DeleteCommuneComponent } from './delete-commune/delete-commune.component';


@NgModule({
  declarations: [
    CommuneComponent,
    CreateCommuneComponent,
    DeleteCommuneComponent
  ],
  imports: [
    SharedModule,
    CommuneRoutingModule,
  ]
})
export class CommuneModule { }
