import { NgModule } from '@angular/core';
import { DepotoirRoutingModule } from './depotoir-routing.module';
import { DepotoirComponent } from './main-content/depotoir.component';
import { CreateDepotoirComponent } from './create-depotoir/create-depotoir.component';
import { DeleteDepotoirComponent } from './delete-depotoir/delete-depotoir.component';
import { SharedModule } from '../../shared/shared.module';


@NgModule({
  declarations: [
    DepotoirComponent,
    CreateDepotoirComponent,
    DeleteDepotoirComponent
  ],
  imports: [
    SharedModule,
    DepotoirRoutingModule,
  ]
})
export class DepotoirModule { }
