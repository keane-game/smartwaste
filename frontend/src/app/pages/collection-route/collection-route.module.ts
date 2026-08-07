import { NgModule } from '@angular/core';

import { CollectionRouteRoutingModule } from './collection-route-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { CollectionRouteComponent } from './main-content/collection-route.component';

@NgModule({
  declarations: [
    CollectionRouteComponent,
  ],
  imports: [
    SharedModule,
    CollectionRouteRoutingModule
  ]
})
export class CollectionRouteModule { }
