import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { SharedService } from '../../services/shared.service';
import { headerTitleService } from '../../services/headerTitle.service';
import { AlertStreamService } from '../../services/alert-stream.service';
import { API_ENDPOINTS } from '../../shared/constants/api-endpoints';

interface FeedEntry {
  alertId?: string;
  object?: string;
  message?: string;
  address?: string;
  code?: string;
  /** Vrai pour une alerte arrivée par le flux SSE pendant que l'écran est ouvert. */
  live: boolean;
  receivedAt?: Date;
}

/**
 * Fil des notifications : les alertes récentes, plus celles qui arrivent en direct par le flux
 * SSE (`/v1/alerts/stream`) tant que l'écran reste ouvert.
 *
 * <p>Cet écran était entièrement cassé : il chargeait bien les ALERTES, mais son tableau
 * affichait des colonnes de DÉPOTOIR (`element.typeDepotoir.typeDepotoirName`,
 * `element.quartier.quartierName` — des champs qui n'existent pas sur une alerte, donc une
 * exception à chaque ligne rendue), son bouton « Ajouter Dépotoir » appelait
 * `OpenCreateCommuneModal()` qui faisait `throw new Error('Method not implemented.')`, et sa
 * suppression visait `element.depotoirId`, toujours `undefined`. Reconstruit en fil d'activité,
 * ce qui correspond à ce que le backend expose réellement (aucun endpoint de notification
 * dédié n'existe).
 */
@Component({
    selector: 'app-notifcation',
    templateUrl: './notifcation.component.html',
    styleUrl: './notifcation.component.scss',
    standalone: false
})
export class NotifcationComponent implements OnInit, OnDestroy {

  entries: FeedEntry[] = [];
  loading = true;
  private streamSubscription?: Subscription;

  constructor(
    private sharedService: SharedService,
    private headerTitleService: headerTitleService,
    private alertStreamService: AlertStreamService,
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Notifications');

    this.sharedService.url = API_ENDPOINTS.alerts.listPath;
    this.sharedService.getAll().subscribe({
      next: (resp: any[]) => {
        this.entries = (resp ?? []).map(a => ({ ...a, live: false }));
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });

    // `LayoutComponent` ouvre déjà le flux pour les comptes ADMIN/SUPER_ADMIN et affiche un toast ;
    // s'y abonner ici en plus ne rouvre pas de connexion (`connect()` est idempotent) et alimente
    // le fil sans dupliquer le flux réseau.
    this.alertStreamService.connect();
    this.streamSubscription = this.alertStreamService.alerts.subscribe(alert => {
      this.entries = [{ ...alert, live: true, receivedAt: new Date() }, ...this.entries];
    });
  }

  ngOnDestroy(): void {
    this.streamSubscription?.unsubscribe();
  }

  codeClasses(code: string | undefined): string {
    switch (code) {
      case 'DANGER': return 'bg-destructive/10 text-destructive';
      case 'WARNING': return 'bg-warning/10 text-warning';
      default: return 'bg-primary/10 text-primary';
    }
  }
}
