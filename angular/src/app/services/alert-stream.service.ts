import { Injectable, NgZone, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * Client du flux temps réel des alertes (`GET /v1/alerts/stream`, ADR-0007).
 *
 * <p>Implémenté avec `fetch` + `ReadableStream` plutôt qu'avec `EventSource`. L'API
 * `EventSource` du navigateur ne permet pas d'ajouter d'en-tête `Authorization`, or le flux est
 * protégé comme le reste de `/v1/**`. Les alternatives auraient été de passer le JWT en
 * paramètre d'URL (le token finit alors dans les logs d'accès et l'historique) ou d'ouvrir
 * l'endpoint côté backend — deux régressions de sécurité. `fetch` accepte les en-têtes et
 * expose le corps en flux, ce qui permet de lire le SSE tel quel.
 *
 * <p>La reconnexion automatique offerte par `EventSource` est donc réimplémentée ici, avec un
 * back-off simple.
 */
@Injectable({ providedIn: 'root' })
export class AlertStreamService implements OnDestroy {

  private readonly alerts$ = new Subject<any>();
  private controller?: AbortController;
  private retryDelayMs = 3000;
  private stopped = false;

  constructor(private zone: NgZone) { }

  /** Flux des alertes poussées par le backend. */
  get alerts(): Observable<any> {
    return this.alerts$.asObservable();
  }

  /** Ouvre le flux. Sans effet si un flux est déjà ouvert. */
  connect(): void {
    if (this.controller) { return; }
    this.stopped = false;
    void this.openStream();
  }

  disconnect(): void {
    this.stopped = true;
    this.controller?.abort();
    this.controller = undefined;
  }

  ngOnDestroy(): void {
    this.disconnect();
    this.alerts$.complete();
  }

  private async openStream(): Promise<void> {
    this.controller = new AbortController();
    const token = localStorage.getItem('access_token');

    try {
      const response = await fetch(`${environment.apiUrl}/alerts/stream`, {
        headers: {
          Accept: 'text/event-stream',
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        signal: this.controller.signal
      });

      if (!response.ok || !response.body) {
        throw new Error(`Flux indisponible (HTTP ${response.status})`);
      }

      this.retryDelayMs = 3000; // connexion réussie : on repart d'un délai court
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) { break; }
        buffer += decoder.decode(value, { stream: true });

        // Les événements SSE sont séparés par une ligne vide ; le dernier fragment
        // (potentiellement incomplet) reste dans le tampon jusqu'au prochain chunk.
        const frames = buffer.split('\n\n');
        buffer = frames.pop() ?? '';
        for (const frame of frames) {
          this.handleFrame(frame);
        }
      }
    } catch (error) {
      if (this.stopped) { return; }
    }

    this.controller = undefined;
    if (!this.stopped) {
      // Reconnexion avec back-off plafonné à 30 s.
      setTimeout(() => this.connect(), this.retryDelayMs);
      this.retryDelayMs = Math.min(this.retryDelayMs * 2, 30000);
    }
  }

  private handleFrame(frame: string): void {
    let eventName = 'message';
    const dataLines: string[] = [];

    for (const line of frame.split('\n')) {
      if (line.startsWith('event:')) { eventName = line.slice(6).trim(); }
      else if (line.startsWith('data:')) { dataLines.push(line.slice(5).trim()); }
    }
    if (eventName !== 'alert' || dataLines.length === 0) { return; }

    const raw = dataLines.join('\n');
    let payload: any;
    try {
      payload = JSON.parse(raw);
    } catch {
      payload = raw; // le backend peut pousser une charge non JSON
    }

    // `fetch` s'exécute hors de la zone Angular : sans `zone.run`, la vue ne serait
    // pas rafraîchie à l'arrivée d'une alerte.
    this.zone.run(() => this.alerts$.next(payload?.alert ?? payload));
  }
}
