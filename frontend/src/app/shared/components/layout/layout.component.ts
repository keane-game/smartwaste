import { Component, ElementRef, Renderer2 ,AfterContentChecked, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Subscription } from 'rxjs';
import Swal from 'sweetalert2';
import { AuthService } from '../../../core/services/auth.service';
import { AlertStreamService } from '../../../services/alert-stream.service';

declare var $: any;

@Component({
    selector: 'app-layout',
    templateUrl: './layout.component.html',
    styleUrls: ['./layout.component.scss'],
    standalone: false
})
export class LayoutComponent implements AfterContentChecked, OnInit, OnDestroy {
isClicked = false;

  private alertsSubscription?: Subscription;

  constructor(
    private elRef: ElementRef,
    private renderer: Renderer2,
    private changeDetector: ChangeDetectorRef,
    private authService: AuthService,
    private alertStreamService: AlertStreamService,
  ) {}

  ngOnInit(): void {
    // `GET /v1/alerts/stream` exige ADMIN ou SUPER_ADMIN (SecurityConfiguration) — se
    // connecter sans vérifier le rôle enverrait chaque session non-admin dans une boucle de
    // reconnexion 403 perpétuelle (le service refait une tentative avec un back-off jusqu'à 30s).
    if (!this.isAdmin()) {
      return;
    }
    this.alertStreamService.connect();
    this.alertsSubscription = this.alertStreamService.alerts.subscribe(alert => {
      const code = alert?.code ?? 'INFO';
      Swal.fire({
        toast: true,
        position: 'top-end',
        icon: code === 'DANGER' ? 'error' : code === 'WARNING' ? 'warning' : 'info',
        title: alert?.object ?? 'Nouvelle alerte',
        text: alert?.address ?? undefined,
        showConfirmButton: false,
        timer: 5000,
      });
    });
  }

  ngOnDestroy(): void {
    this.alertsSubscription?.unsubscribe();
    this.alertStreamService.disconnect();
  }

  private isAdmin(): boolean {
    const token = this.authService.getAuthToken();
    if (!token) {
      return false;
    }
    try {
      const decoded = new JwtHelperService().decodeToken(token);
      const roles: any[] = decoded?.role ?? [];
      return roles.some(r => {
        const name = typeof r === 'string' ? r : r?.authority;
        return name === 'ROLE_ADMIN' || name === 'ROLE_SUPER_ADMIN';
      });
    } catch {
      return false;
    }
  }



  handleSidebarToggles(event: any): void{
    this.isClicked = !this.isClicked;
    const el = this.elRef.nativeElement.querySelector('#sidebar');
    const el2 = this.elRef.nativeElement.querySelectorAll('.main-content');
    const body = this.elRef.nativeElement.querySelector('.main-body');
    console.log("ell:"+ el)
    el2.forEach((element: any) => {
     
        this.renderer.removeClass(element, 'toggle-sidebar');
    });
    if(this.isClicked){
      this.renderer.addClass(body, 'toggle-sidebar');
      el2.forEach((element: any) => {
    });
    }else{
      this.renderer.removeClass(body, 'toggle-sidebar');
      el2.forEach((element: any) => {
    });
    }
    //console.log("toggle-sidebar-btn")
  }



  ngAfterContentChecked(): void {
    this.changeDetector.detectChanges();
  }

}
