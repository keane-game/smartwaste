/**
 * État d'interface partagé par les écrans de liste (menu `…`, sélection, bornes de pagination).
 *
 * <p>Conçu comme une simple classe instanciée en champ (`ui = new ListUiState()`) plutôt qu'en
 * classe de base à hériter : les composants de liste sont déjà des composants Angular avec leur
 * propre injection, et l'héritage aurait imposé de remonter les dépendances dans chaque
 * constructeur pour un gain nul.
 *
 * <p>La position du menu est calculée à l'ouverture et rendue en `position: fixed` par le
 * template : le conteneur de tableau est en `overflow-x`, et dès qu'un axe n'est pas `visible`
 * l'autre passe à `auto` — un menu en `absolute` dans la cellule était rogné et restait invisible
 * bien que présent dans le DOM (constaté au navigateur avant correction).
 */
export class ListUiState {

  /** Identifiant de la ligne dont le menu est ouvert (`null` = aucun). */
  openRowMenu: string | null = null;

  /** Position du menu en coordonnées de fenêtre. */
  rowMenuPos = { top: 0, left: 0 };

  /** Lignes cochées. */
  readonly selectedIds = new Set<string>();

  /** Largeur du menu, doit rester alignée sur `.sw-menu` (components.css). */
  private static readonly MENU_WIDTH = 180;

  toggleRowMenu(id: string, event: MouseEvent): void {
    if (this.openRowMenu === id) {
      this.openRowMenu = null;
      return;
    }
    const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
    this.rowMenuPos = { top: rect.bottom + 4, left: rect.right - ListUiState.MENU_WIDTH };
    this.openRowMenu = id;
  }

  closeRowMenu(): void {
    this.openRowMenu = null;
  }

  toggleSelection(id: string): void {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
  }

  isSelected(id: string): boolean {
    return this.selectedIds.has(id);
  }

  allSelected(rows: any[], idField: string): boolean {
    return rows.length > 0 && rows.every(r => this.selectedIds.has(r[idField]));
  }

  toggleSelectAll(rows: any[], idField: string): void {
    if (this.allSelected(rows, idField)) {
      this.selectedIds.clear();
    } else {
      rows.forEach(r => this.selectedIds.add(r[idField]));
    }
  }

  /** Borne basse affichée par la pagination (« 1-10 sur 12 »). */
  rangeStart(currentPage: number, pageSize: number, total: number): number {
    return total === 0 ? 0 : currentPage * pageSize + 1;
  }

  rangeEnd(currentPage: number, pageSize: number, total: number): number {
    return Math.min((currentPage + 1) * pageSize, total);
  }
}
