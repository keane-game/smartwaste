import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ModalService } from '../../services/modal.service';
import { ColumnManagerComponent, ManagedColumn } from '../components/column-manager/column-manager.component';

/**
 * Préférences de colonnes d'un écran de liste : restauration, validation, ouverture du dialogue
 * et persistance.
 *
 * <p>Extrait des composants parce que six listes partagent exactement la même mécanique — la
 * dupliquer aurait signifié six copies de la revalidation et six clés de stockage écrites à la
 * main. Chaque écran ne déclare plus que son inventaire de colonnes et sa clé.
 */
@Injectable({ providedIn: 'root' })
export class ColumnPreferencesService {

  constructor(private modalService: ModalService) { }

  /**
   * Colonnes à afficher au chargement : le choix mémorisé s'il est encore valide, sinon le défaut.
   *
   * <p>Le choix stocké est REVALIDÉ contre l'inventaire courant : une colonne renommée ou retirée
   * du code ne doit pas casser l'écran au prochain chargement, et les colonnes verrouillées
   * (sélection, actions) doivent toujours être présentes — un stockage trafiqué ou hérité d'une
   * version antérieure priverait sinon l'écran de ses actions.
   */
  restore(key: string, columns: ManagedColumn[], hiddenByDefault: string[] = []): string[] {
    try {
      const stored = JSON.parse(localStorage.getItem(key) || 'null');
      if (Array.isArray(stored)) {
        const known = stored.filter((k: unknown): k is string =>
          typeof k === 'string' && columns.some(c => c.key === k));
        const locked = columns.filter(c => c.locked).map(c => c.key);
        if (known.length > 0 && locked.every(k => known.includes(k))) {
          return known;
        }
      }
    } catch {
      // Stockage illisible : on repart du défaut plutôt que d'échouer au chargement de l'écran.
    }
    return this.defaults(columns, hiddenByDefault);
  }

  private defaults(columns: ManagedColumn[], hiddenByDefault: string[]): string[] {
    return columns.map(c => c.key).filter(k => !hiddenByDefault.includes(k));
  }

  /**
   * Ouvre « Gérer les colonnes » et persiste le résultat.
   *
   * <p>Émet la nouvelle liste, ou `null` si l'utilisateur a annulé — l'appelant ne met à jour son
   * tableau que dans le premier cas.
   */
  open(key: string, columns: ManagedColumn[], current: string[]): Observable<string[] | null> {
    return this.modalService
      .openModal<ColumnManagerComponent>(ColumnManagerComponent,
        { columns, visible: current },
        { panelClass: [], maxWidth: '720px', width: '100%' })
      .afterClosed()
      .pipe(map((chosen: string[] | null) => {
        if (chosen) {
          localStorage.setItem(key, JSON.stringify(chosen));
        }
        return chosen ?? null;
      }));
  }
}
