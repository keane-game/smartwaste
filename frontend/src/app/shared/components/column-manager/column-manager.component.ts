import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

/** Une colonne pilotable par l'utilisateur. `locked` = toujours visible (sélection, actions). */
export interface ManagedColumn {
  key: string;
  label: string;
  locked?: boolean;
}

export interface ColumnManagerData {
  /** Toutes les colonnes proposées, dans l'ordre par défaut. */
  columns: ManagedColumn[];
  /** Clés actuellement visibles, dans l'ordre d'affichage courant. */
  visible: string[];
}

/**
 * « Gérer les colonnes à afficher » (maquettes de référence, frontend/mockup).
 *
 * <p>Deux panneaux : à gauche les colonnes visibles, réordonnables par glisser-déposer ; à droite
 * l'inventaire complet avec une case par colonne. Les colonnes techniques (case à cocher de
 * sélection, menu d'actions) sont marquées `locked` : les masquer priverait l'écran de ses
 * actions sans que l'utilisateur comprenne pourquoi.
 *
 * <p>Le dialogue ne connaît que des clés et des libellés : il ne sait rien de la ressource
 * affichée, ce qui permet de le brancher sur n'importe quelle liste.
 */
@Component({
  selector: 'app-column-manager',
  standalone: true,
  imports: [CommonModule, MatDialogModule, DragDropModule],
  templateUrl: './column-manager.component.html',
})
export class ColumnManagerComponent {

  readonly columns: ManagedColumn[];

  /**
   * Colonnes visibles, dans l'ordre — la source de vérité du panneau de gauche.
   *
   * <p>Nommée `visibleColumns` et NON `visible` : `ModalService.openModal` recopie les données du
   * dialogue sur l'instance (`Object.assign`, nécessaire aux anciens formulaires qui lisent des
   * champs d'instance plutôt que `MAT_DIALOG_DATA`). Un champ homonyme de la charge de données
   * était donc écrasé — ici par le tableau de CHAÎNES `visible`, ce qui faisait enregistrer des
   * colonnes vides (`[null, null, …]`) et cassait le tableau.
   */
  visibleColumns: ManagedColumn[];

  private readonly initialVisible: string[];

  constructor(
    private dialogRef: MatDialogRef<ColumnManagerComponent>,
    @Inject(MAT_DIALOG_DATA) data: ColumnManagerData,
  ) {
    this.columns = data.columns;
    this.initialVisible = [...data.visible];
    this.visibleColumns = this.toColumns(data.visible);
  }

  private toColumns(keys: string[]): ManagedColumn[] {
    return keys
      .map(k => this.columns.find(c => c.key === k))
      .filter((c): c is ManagedColumn => !!c);
  }

  /** Colonnes réordonnables : les verrouillées gardent leur place. */
  get reorderable(): ManagedColumn[] {
    return this.visibleColumns.filter(c => !c.locked);
  }

  isVisible(column: ManagedColumn): boolean {
    return this.visibleColumns.some(c => c.key === column.key);
  }

  toggle(column: ManagedColumn): void {
    if (column.locked) {
      return;
    }
    if (this.isVisible(column)) {
      this.visibleColumns = this.visibleColumns.filter(c => c.key !== column.key);
      return;
    }
    // Réinsérée à sa position d'origine plutôt qu'en fin de liste : recocher une colonne ne doit
    // pas réorganiser silencieusement le tableau.
    const target = this.columns.indexOf(column);
    const before = this.columns.slice(0, target).map(c => c.key);
    const index = this.visibleColumns.findIndex(c => !before.includes(c.key));
    this.visibleColumns = index === -1
      ? [...this.visibleColumns, column]
      : [...this.visibleColumns.slice(0, index), column, ...this.visibleColumns.slice(index)];
  }

  drop(event: CdkDragDrop<ManagedColumn[]>): void {
    // `reorderable` est un getter qui refiltre à chaque accès : le réutiliser après la mutation
    // recalculait une NOUVELLE liste et la recomposition tombait en décalage, produisant des
    // trous (`null`) dans les colonnes enregistrées. On fige donc une copie locale.
    const reordered = this.visibleColumns.filter(c => !c.locked);
    moveItemInArray(reordered, event.previousIndex, event.currentIndex);

    let i = 0;
    this.visibleColumns = this.visibleColumns.map(c => c.locked ? c : reordered[i++]);
  }

  reset(): void {
    this.visibleColumns = this.toColumns(this.columns.map(c => c.key));
  }

  get dirty(): boolean {
    const current = this.visibleColumns.map(c => c.key);
    return current.length !== this.initialVisible.length
      || current.some((k, i) => k !== this.initialVisible[i]);
  }

  save(): void {
    this.dialogRef.close(this.visibleColumns.map(c => c.key));
  }
}
