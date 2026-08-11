import { HttpClient } from '@angular/common/http';
import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../../shared/constants/api-endpoints';
import { SessionService } from './session.service';

export interface Organization {
  organizationId: string;
  name: string;
  code: string;
  status: 'ACTIVE' | 'SUSPENDED';
}

/**
 * Périmètre d'observation du SUPER_ADMIN.
 *
 * <p><b>Ce que résout cet écran.</b> Le backend cloisonne les données par collectivité : un ADMIN
 * ne voit que la sienne, un SUPER_ADMIN voit tout. Mais « voir tout » n'est pas toujours ce qu'on
 * veut — superviser une collectivité en particulier imposait jusqu'ici de changer de compte. Le
 * serveur accepte pour cela un en-tête `X-Organization-Id` ; ce service en tient le choix, et
 * {@code TenantScopeInterceptor} le pose sur chaque requête.
 *
 * <p><b>L'en-tête n'a d'effet que pour un SUPER_ADMIN.</b> Le serveur l'ignore délibérément pour
 * tout autre rôle — le lire donnerait à n'importe quel ADMIN le moyen de consulter une autre
 * collectivité en ajoutant une ligne à sa requête. Ce service ne l'envoie donc pas non plus : ce
 * n'est pas la sécurité (elle est côté serveur), c'est la cohérence — une interface qui propose un
 * choix sans effet est une interface qui ment.
 *
 * <p><b>Le choix est rattaché au compte.</b> La clé de stockage porte l'e-mail : deux comptes
 * successifs sur le même navigateur ne se transmettent pas un périmètre, et une déconnexion ne
 * laisse pas traîner celui du précédent.
 */
@Injectable({ providedIn: 'root' })
export class OrganizationScopeService {

  /** Doit rester identique à `TenantFilterActivationFilter.SCOPE_HEADER` côté backend. */
  static readonly SCOPE_HEADER = 'X-Organization-Id';

  private readonly http = inject(HttpClient);
  private readonly sessionService = inject(SessionService);

  private readonly organizationsSignal = signal<Organization[]>([]);
  /** `null` = vue plateforme complète, aucun en-tête envoyé. */
  private readonly selectedIdSignal = signal<string | null>(null);
  private loaded = false;

  readonly organizations = this.organizationsSignal.asReadonly();
  readonly selectedId = this.selectedIdSignal.asReadonly();

  /** Le sélecteur n'a de sens — et l'en-tête d'effet — que pour l'exploitant de la plateforme. */
  readonly canSelectScope = computed(() => this.sessionService.hasAnyRole('SUPER_ADMIN'));

  readonly selected = computed(() =>
    this.organizationsSignal().find(o => o.organizationId === this.selectedIdSignal()) ?? null);

  readonly label = computed(() => this.selected()?.name ?? 'Toutes les collectivités');

  /** Compte pour lequel l'état courant a été chargé, afin de détecter un changement de session. */
  private currentEmail: string | null = null;

  constructor() {
    // Réagit au compte plutôt que d'être appelé par `AuthService.logout()` : celui-ci ne peut pas
    // injecter ce service — il passerait par `SessionService`, qui dépend de lui, donc un cycle de
    // DI. L'effet couvre du même coup la connexion, la déconnexion et l'enchaînement de deux
    // comptes sur le même navigateur.
    effect(() => {
      const email = this.sessionService.user()?.email ?? null;
      if (email === this.currentEmail) {
        return;
      }
      this.currentEmail = email;
      this.loaded = false;
      this.organizationsSignal.set([]);
      // Le choix est relu pour le NOUVEAU compte : la clé porte l'e-mail, donc changer de compte
      // ne transmet jamais le périmètre du précédent.
      this.selectedIdSignal.set(email ? localStorage.getItem(this.storageKey()) : null);
      this.load();
    });
  }

  /**
   * En-tête à poser, ou `null` s'il ne faut rien poser.
   *
   * <p>Volontairement synchrone et sans effet de bord : l'intercepteur s'exécute sur chaque
   * requête, il ne peut pas attendre un chargement.
   */
  scopeHeader(): string | null {
    return this.canSelectScope() ? this.selectedIdSignal() : null;
  }

  /**
   * Charge la liste des collectivités, une seule fois.
   *
   * <p>Un échec est avalé : l'endpoint répond 403 à tout compte sans `MANAGE_ORGANIZATIONS`, ce
   * qui est le cas normal et non une anomalie à signaler à l'utilisateur. Le sélecteur reste alors
   * simplement vide — il n'est de toute façon pas affiché hors SUPER_ADMIN.
   */
  load(): void {
    if (this.loaded || !this.canSelectScope()) {
      return;
    }
    this.loaded = true;
    this.http.get<Organization[]>(environment.apiUrl + API_ENDPOINTS.organizations.listPath)
      .subscribe({
        next: orgs => {
          const actives = (orgs ?? []).filter(o => o.status === 'ACTIVE');
          this.organizationsSignal.set(actives);
          // Un périmètre mémorisé qui ne correspond plus à une collectivité active (suspendue,
          // renommée, supprimée côté serveur) doit retomber sur la vue complète, pas rester posé
          // sur un identifiant fantôme qui ne rendrait plus aucune donnée.
          const current = this.selectedIdSignal();
          if (current && !actives.some(o => o.organizationId === current)) {
            this.select(null);
          }
        },
        error: () => this.organizationsSignal.set([]),
      });
  }

  select(organizationId: string | null): void {
    this.selectedIdSignal.set(organizationId);
    if (organizationId) {
      localStorage.setItem(this.storageKey(), organizationId);
    } else {
      localStorage.removeItem(this.storageKey());
    }
  }

  private storageKey(): string {
    return `organizationScope:${this.sessionService.user()?.email ?? 'anonyme'}`;
  }
}
