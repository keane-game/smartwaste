import { Component, OnInit } from '@angular/core';
import { SharedService } from '../../../services/shared.service';

/**
 * Liste des dépotoirs + corbeille (soft-delete).
 *
 * Réutilise le service existant {@link SharedService} : `delete()` déclenche la suppression
 * logique côté backend, `getDeletions()` liste les éléments en attente de purge, `restore()`
 * les restaure tant que le délai de rétention (30 j) court.
 */
@Component({
  selector: 'app-list-depotoir',
  templateUrl: './list-depotoir.component.html',
  styleUrls: ['./list-depotoir.component.scss']
})
export class ListDepotoirComponent implements OnInit {

  depotoirs: any[] = [];
  deletions: any[] = [];
  loading = false;
  message = '';
  showTrash = false;

  constructor(private sharedService: SharedService) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.sharedService.url = '/depotoirss'; // liste des dépotoirs actifs
    this.sharedService.getAll().subscribe({
      next: (data) => { this.depotoirs = data || []; this.loading = false; },
      error: () => { this.loading = false; this.message = 'Erreur de chargement des dépotoirs.'; }
    });
  }

  loadDeletions(): void {
    this.sharedService.url = '/depotoirs';
    this.sharedService.getDeletions().subscribe({
      next: (data) => { this.deletions = data || []; },
      error: () => { this.message = 'Erreur de chargement de la corbeille.'; }
    });
  }

  toggleTrash(): void {
    this.showTrash = !this.showTrash;
    if (this.showTrash) { this.loadDeletions(); }
  }

  softDelete(d: any): void {
    if (!confirm(`Supprimer le dépotoir #${d.depotoirId} ? Il restera récupérable 30 jours.`)) { return; }
    this.sharedService.url = '/depotoirs';
    this.sharedService.delete(d.depotoirId).subscribe({
      next: () => {
        this.message = 'Dépotoir déplacé vers la corbeille (récupérable 30 jours).';
        this.load();
        if (this.showTrash) { this.loadDeletions(); }
      },
      error: () => { this.message = 'Échec de la suppression.'; }
    });
  }

  restore(d: any): void {
    this.sharedService.url = '/depotoirs';
    this.sharedService.restore(d.depotoirId).subscribe({
      next: () => {
        this.message = 'Dépotoir restauré.';
        this.loadDeletions();
        this.load();
      },
      error: () => { this.message = 'Restauration impossible (délai de rétention dépassé ?).'; }
    });
  }
}
