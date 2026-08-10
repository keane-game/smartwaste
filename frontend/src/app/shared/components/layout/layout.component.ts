import { Component, ElementRef, Renderer2 ,AfterContentChecked, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import Swal from 'sweetalert2';
import { SessionService } from '../../../core/services/session.service';
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
    private sessionService: SessionService,
    private alertStreamService: AlertStreamService,
  ) {}

  ngOnInit(): void {
    // `GET /v1/alerts/stream` exige ADMIN ou SUPER_ADMIN (SecurityConfiguration) — se
    // connecter sans vérifier le rôle enverrait chaque session non-admin dans une boucle de
    // reconnexion 403 perpétuelle (le service refait une tentative avec un back-off jusqu'à 30s).
    //
    // Corrige un bug réel trouvé pendant la refonte (Phase 0/2) : l'ancienne version décodait le
    // JWT à la main et traitait `role` comme un tableau (`roles.some(...)`), alors que le claim
    // est une chaîne unique (`"ROLE_SUPER_ADMIN"`) — vérifié sur un jeton réel. `.some` n'existe
    // pas sur une chaîne, l'appel levait une exception silencieusement rattrapée : `isAdmin()`
    // renvoyait toujours `false`, pour tout le monde. Le flux temps réel ne s'est donc jamais
    // connecté, y compris pour un compte SUPER_ADMIN, depuis son introduction.
    if (!this.sessionService.hasAnyRole('ADMIN', 'SUPER_ADMIN')) {
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
