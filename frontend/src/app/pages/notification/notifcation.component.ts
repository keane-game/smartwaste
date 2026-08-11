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
  /** Marquée comme lue par l'utilisateur (état local, l'API ne porte pas cette notion). */
  read?: boolean;
}

/** Onglet du fil : `all` = tout, `unread` = non lues, `live` = arrivées pendant la session. */
type FeedTab = 'all' | 'unread' | 'live';

/**
 * Fil des notifications : les alertes récentes, plus celles qui arrivent en direct par le flux
 * SSE (`/v1/alerts/stream`) tant que l'écran reste ouvert.
 *
 * <p>Reprend le tiroir des maquettes (frontend/mockup, « notifs ») : onglets soulignés en tête,
 * filtres segmentés par gravité, puis la liste. Le tri « lues / non lues » est LOCAL : le backend
 * n'expose aucune notion de lecture — la marquer côté client est honnête tant qu'on ne prétend pas
 * la partager entre appareils.
 *
 * <p>Cet écran était entièrement cassé avant reconstruction : il chargeait bien les ALERTES, mais
 * affichait des colonnes de DÉPOTOIR (`element.typeDepotoir.typeDepotoirName`…, champs absents
 * d'une alerte, donc une exception par ligne rendue) et ses boutons appelaient des méthodes qui
 * faisaient `throw new Error('Method not implemented.')`.
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

  activeTab: FeedTab = 'all';
  /** Filtre de gravité — `null` = toutes. */
  activeCode: string | null = null;

  readonly codes = [
    { label: 'Toutes les activités', code: null },
    { label: 'Danger', code: 'DANGER' },
    { label: 'Avertissement', code: 'WARNING' },
    { label: 'Information', code: 'INFO' },
  ];

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
        this.entries = (resp ?? []).map(a => ({ ...a, live: false, read: false }));
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });

    // `LayoutComponent` ouvre déjà le flux pour les comptes ADMIN/SUPER_ADMIN ; s'y abonner ici
    // ne rouvre pas de connexion (`connect()` est idempotent) et alimente le fil sans dupliquer
    // le trafic réseau.
    this.alertStreamService.connect();
    this.streamSubscription = this.alertStreamService.alerts.subscribe(alert => {
      this.entries = [{ ...alert, live: true, read: false, receivedAt: new Date() }, ...this.entries];
    });
  }

  ngOnDestroy(): void {
    this.streamSubscription?.unsubscribe();
  }

  get visible(): FeedEntry[] {
    return this.entries.filter(e => {
      if (this.activeTab === 'unread' && e.read) { return false; }
      if (this.activeTab === 'live' && !e.live) { return false; }
      if (this.activeCode !== null && e.code !== this.activeCode) { return false; }
      return true;
    });
  }

  countFor(tab: FeedTab): number {
    if (tab === 'unread') { return this.entries.filter(e => !e.read).length; }
    if (tab === 'live') { return this.entries.filter(e => e.live).length; }
    return this.entries.length;
  }

  selectTab(tab: FeedTab): void {
    this.activeTab = tab;
  }

  selectCode(code: string | null): void {
    this.activeCode = code;
  }

  markRead(entry: FeedEntry): void {
    entry.read = true;
  }

  markAllRead(): void {
    this.entries.forEach(e => e.read = true);
  }

  codeClasses(code: string | undefined): string {
    switch (code) {
      case 'DANGER': return 'sw-badge sw-badge--danger';
      case 'WARNING': return 'sw-badge sw-badge--warn';
      default: return 'sw-badge sw-badge--ok';
    }
  }
}
