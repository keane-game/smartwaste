import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';

import { QuartierRoutingModule } from './quartier-routing.module';
import { QuartierComponent } from './main-content/quartier.component';
import { CreateQuartierComponent } from './create-quartier/create-quartier.component';
import { SharedModule } from '../../shared/shared.module';
import { DeleteQuartierComponent } from './delete-quartier/delete-quartier.component';


@NgModule({
  declarations: [
    QuartierComponent,
    DeleteQuartierComponent,
    CreateQuartierComponent
  ],
  imports: [
    QuartierRoutingModule,
    SharedModule,
  ],
})
export class QuartierModule { }
