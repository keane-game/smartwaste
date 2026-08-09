import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { headerTitleService } from '../../../services/headerTitle.service';
import { environment } from '../../../../environments/environment';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';
import { succesAlert, errorAlert } from '../../../services/alert.service';

interface Vehicle {
  vehicleId: string;
  registration: string;
  label: string;
  circuitCollectId: string | null;
  active: boolean;
  lastLatitude: number | null;
  lastLongitude: number | null;
  lastPositionAt: string | null;
}

@Component({
    selector: 'app-vehicle',
    templateUrl: './vehicle.component.html',
    styleUrls: ['./vehicle.component.scss'],
    standalone: false
})
export class VehicleComponent implements OnInit {

  private readonly baseUrl = `${environment.apiUrl}${API_ENDPOINTS.vehicles.basePath}`;

  vehicles: Vehicle[] = [];
  loading = false;
  submitting = false;

  editingId: string | null = null;
  form = { registration: '', label: '' };

  constructor(
    private http: HttpClient,
    private headerTitleService: headerTitleService,
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Flotte');
    this.load();
  }

  load(): void {
    this.loading = true;
    this.http.get<Vehicle[]>(this.baseUrl).subscribe({
      next: (vehicles) => { this.vehicles = vehicles; this.loading = false; },
      error: () => { this.loading = false; errorAlert('Impossible de charger la flotte.'); }
    });
  }

  startEdit(vehicle: Vehicle): void {
    this.editingId = vehicle.vehicleId;
    this.form = { registration: vehicle.registration, label: vehicle.label };
  }

  cancelEdit(): void {
    this.editingId = null;
    this.form = { registration: '', label: '' };
  }

  submit(): void {
    if (!this.form.registration.trim() || !this.form.label.trim()) {
      return;
    }
    this.submitting = true;
    const request$ = this.editingId === null
      ? this.http.post<Vehicle>(this.baseUrl, this.form)
      : this.http.put<Vehicle>(`${this.baseUrl}/${this.editingId}`, this.form);

    request$.subscribe({
      next: () => {
        this.submitting = false;
        succesAlert(this.editingId === null ? 'Véhicule ajouté.' : 'Véhicule modifié.');
        this.cancelEdit();
        this.load();
      },
      error: () => {
        this.submitting = false;
        errorAlert("Échec de l'enregistrement.");
      }
    });
  }

  deactivate(vehicle: Vehicle): void {
    if (!confirm(`Retirer « ${vehicle.registration} » de la circulation ?`)) {
      return;
    }
    this.http.delete(`${this.baseUrl}/${vehicle.vehicleId}`).subscribe({
      next: () => { succesAlert('Véhicule retiré de la circulation.'); this.load(); },
      error: () => { errorAlert('Échec de l\'opération.'); }
    });
  }

}
