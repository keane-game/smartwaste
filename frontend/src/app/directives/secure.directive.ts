import {Directive, Input, TemplateRef, ViewContainerRef} from '@angular/core';

import {KeycloakOperationService} from "../core/services/keycloack.service";

@Directive({
  selector: '[secure]'
})
export class SecureDirective {

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    //private keycloakService: KeycloakOperationService
  ) {}

  @Input() set secure(roles: string[]) {
    const userRoles: string | string[] = [] //this.keycloakService.getUserRoles();
    if (roles.some(role => userRoles.includes(role))) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else {
      this.viewContainer.clear();
    }
  }
}



