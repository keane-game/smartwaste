import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';

/**
 * Gestion des circuits de collecte — liste + création/édition inline + suppression.
 * Backend `/v1/circuit-collects` (liste `GET`, CRUD complet).
 */
@Component({
  selector: 'app-list-circuit-collect',
  templateUrl: './list-circuit-collect.component.html',
  styleUrls: ['./list-circuit-collect.component.scss']
})
export class ListCircuitCollectComponent implements OnInit {

  private readonly resource = '/circuit-collects';

  items: any[] = [];
  communes: any[] = [];
  loading = false;
  saving = false;
  message = '';
  error = '';

  form!: FormGroup;
  showForm = false;
  editingId: number | null = null;

  constructor(
    private sharedService: SharedService,
    private formBuilder: FormBuilder,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Gestion Circuit de collecte');
    this.form = this.formBuilder.group({
      name: ['', Validators.required],
      code: [''],
      length: [''],
      frequency: [''],
      type: [''],
      cat: [''],
      rotation: [''],
      section: [''],
      sectection: [''],
      latiPointA: [''],
      longPointA: [''],
      latiPointD: [''],
      longPointD: [''],
      commune: [null]
    });
    this.load();
    this.loadCommunes();
  }

  get f() { return this.form.controls; }

  load(): void {
    this.loading = true;
    this.sharedService.url = this.resource;
    this.sharedService.getAll().subscribe({
      next: (data) => { this.items = data || []; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Erreur de chargement des circuits de collecte.'; }
    });
  }

  loadCommunes(): void {
    this.sharedService.url = '/communes/s';
    this.sharedService.getAll().subscribe({
      next: (data) => { this.communes = data || []; },
      error: () => { /* non bloquant */ }
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form.reset();
    this.showForm = true;
    this.message = '';
    this.error = '';
  }

  openEdit(item: any): void {
    this.editingId = item.circuitcollectId;
    this.form.patchValue({
      name: item.name, code: item.code, length: item.length, frequency: item.frequency,
      type: item.type, cat: item.cat, rotation: item.rotation,
      section: item.section, sectection: item.sectection,
      latiPointA: item.latiPointA, longPointA: item.longPointA,
      latiPointD: item.latiPointD, longPointD: item.longPointD,
      commune: item.commune ?? null
    });
    this.showForm = true;
    this.message = '';
    this.error = '';
  }

  cancel(): void {
    this.showForm = false;
    this.editingId = null;
    this.form.reset();
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.sharedService.url = this.resource;
    const payload = this.form.value;
    const request = this.editingId
      ? this.sharedService.update(payload, this.editingId)
      : this.sharedService.create(payload);
    request.subscribe({
      next: () => {
        this.saving = false;
        this.message = this.editingId ? 'Circuit de collecte mis à jour.' : 'Circuit de collecte créé.';
        this.cancel();
        this.load();
      },
      error: () => { this.saving = false; this.error = 'Échec de l\'enregistrement.'; }
    });
  }

  remove(item: any): void {
    if (!confirm(`Supprimer le circuit de collecte « ${item.name} » ?`)) { return; }
    this.sharedService.url = this.resource;
    this.sharedService.delete(item.circuitcollectId).subscribe({
      next: () => { this.message = 'Circuit de collecte supprimé.'; this.load(); },
      error: () => { this.error = 'Échec de la suppression.'; }
    });
  }
}
