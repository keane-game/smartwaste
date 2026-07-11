import { NgModule } from '@angular/core';
import { EntitiesRoutingModule } from './entities-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialsModule } from '../materials/material.module';

import { CommonModule } from '@angular/common';
import { ListUserComponent } from './users/list/list-user.component';
import { UpdateUserComponent } from './users/update/update-user.component';
import { CreateUserComponent } from './users/create/create-user.component';
import { DepotoirComponent } from './depotoir/depotoir.component';
import { CreateDepotoirComponent } from './depotoir/create/create-depotoir.component';
import { ListDepotoirComponent } from './depotoir/list/list-depotoir.component';

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
