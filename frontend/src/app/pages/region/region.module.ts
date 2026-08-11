import { NgModule } from '@angular/core';

import { RegionRoutingModule } from './region-routing.module';

// `RegionComponent`/`CreateRegionComponent` sont standalone (construits depuis un stub CLI vide,
// "region works!" — jamais servi avant) : rien à déclarer ici, `region-routing.module.ts` les
// référence directement.
@NgModule({
  imports: [
    RegionRoutingModule,
  ],
})
export class RegionModule { }
