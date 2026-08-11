import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { headerTitleService } from '../../../services/headerTitle.service';
import { environment } from '../../../../environments/environment';
import { API_ENDPOINTS, API_PATHS } from '../../../shared/constants/api-endpoints';
import { errorAlert } from '../../../services/alert.service';

interface DeviceSummary {
  deviceId: string;
  deviceCode: string;
  target: string;
  active: boolean;
  lastSeenAt: string | null;
  silenceReportedAt: string | null;
  orphaned: boolean;
}

interface ProvisionedDevice {
  deviceId: string;
  deviceCode: string;
  apiKey: string;
}

interface Depotoir {
  depotoirId: string;
  address: string;
}

interface Vehicle {
  vehicleId: string;
  registration: string;
}

/**
 * Provisioning IoT (`/v1/devices/**`, permission `MANAGE_DEVICES`).
 *
 * <p>⚠️ La clé d'API n'est rendue qu'une fois, à l'enrôlement ou à la rotation — jamais relisible
 * ensuite (seule son empreinte est conservée côté serveur). D'où l'alerte affichée une seule fois
 * et explicitement fermée par l'utilisateur, plutôt qu'un simple toast qui disparaît seul.
 */
@Component({
    selector: 'app-device',
    templateUrl: './device.component.html',
    styleUrls: ['./device.component.scss'],
    standalone: false
})
export class DeviceComponent implements OnInit {

  private readonly baseUrl = environment.apiUrl;

  sensors: DeviceSummary[] = [];
  trackers: DeviceSummary[] = [];
  depotoirs: Depotoir[] = [];
  vehicles: Vehicle[] = [];

  loadingSensors = false;
  loadingTrackers = false;

  sensorForm = { deviceCode: '', depotoirId: null as string | null };
  trackerForm = { deviceCode: '', vehicleId: null as string | null };
  enrollingSensor = false;
  enrollingTracker = false;

  /** Clé rendue une seule fois — par enrôlement ou rotation. `null` = rien à montrer. */
  revealedKey: { deviceCode: string; apiKey: string } | null = null;

  constructor(
    private http: HttpClient,
    private headerTitleService: headerTitleService,
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Capteurs & traceurs');
    this.loadSensors();
    this.loadTrackers();
    this.http.get<Depotoir[]>(`${this.baseUrl}${API_ENDPOINTS.depotoirs.listPath}`)
      .subscribe({ next: (depotoirs) => { this.depotoirs = depotoirs; } });
    this.http.get<Vehicle[]>(`${this.baseUrl}${API_ENDPOINTS.vehicles.basePath}`)
      .subscribe({ next: (vehicles) => { this.vehicles = vehicles; } });
  }

  dismissKey(): void {
    this.revealedKey = null;
  }

  /**
   * Libellé lisible du point de collecte / véhicule visé par un équipement.
   *
   * <p>`DeviceSummary.target` est l'IDENTIFIANT brut (`String.valueOf(depotoirId)`), pas un
   * libellé : le contexte `iot` ne peut pas lire les entités de `waste` (référence par
   * identifiant, ADR-0012), il n'a donc pas de quoi composer une adresse côté serveur. La
   * résolution se fait ici, où les deux listes sont déjà chargées pour les menus déroulants —
   * sinon l'écran affichait un UUID nu, illisible pour choisir quel capteur désactiver.
   */
  targetLabel(target: string, kind: 'sensor' | 'tracker'): string {
    if (kind === 'sensor') {
      return this.depotoirs.find(d => d.depotoirId === target)?.address ?? target;
    }
    return this.vehicles.find(v => v.vehicleId === target)?.registration ?? target;
  }

  private loadSensors(): void {
    this.loadingSensors = true;
    this.http.get<DeviceSummary[]>(`${this.baseUrl}${API_PATHS.devices.sensors}`).subscribe({
      next: (sensors) => { this.sensors = sensors; this.loadingSensors = false; },
      error: () => { this.loadingSensors = false; errorAlert('Impossible de charger les capteurs.'); }
    });
  }

  private loadTrackers(): void {
    this.loadingTrackers = true;
    this.http.get<DeviceSummary[]>(`${this.baseUrl}${API_PATHS.devices.vehicleTrackers}`).subscribe({
      next: (trackers) => { this.trackers = trackers; this.loadingTrackers = false; },
      error: () => { this.loadingTrackers = false; errorAlert('Impossible de charger les traceurs.'); }
    });
  }

  enrollSensor(): void {
    if (!this.sensorForm.deviceCode.trim() || this.sensorForm.depotoirId === null) {
      return;
    }
    this.enrollingSensor = true;
    this.http.post<ProvisionedDevice>(`${this.baseUrl}${API_PATHS.devices.enrollSensor}`, this.sensorForm)
      .subscribe({
        next: (device) => {
          this.enrollingSensor = false;
          this.revealedKey = { deviceCode: device.deviceCode, apiKey: device.apiKey };
          this.sensorForm = { deviceCode: '', depotoirId: null };
          this.loadSensors();
        },
        error: () => { this.enrollingSensor = false; errorAlert("Échec de l'enrôlement."); }
      });
  }

  enrollTracker(): void {
    if (!this.trackerForm.deviceCode.trim() || !this.trackerForm.vehicleId) {
      return;
    }
    this.enrollingTracker = true;
    this.http.post<ProvisionedDevice>(`${this.baseUrl}${API_PATHS.devices.enrollVehicleTracker}`, this.trackerForm)
      .subscribe({
        next: (device) => {
          this.enrollingTracker = false;
          this.revealedKey = { deviceCode: device.deviceCode, apiKey: device.apiKey };
          this.trackerForm = { deviceCode: '', vehicleId: null };
          this.loadTrackers();
        },
        error: () => { this.enrollingTracker = false; errorAlert("Échec de l'enrôlement."); }
      });
  }

  rotateSensorKey(sensor: DeviceSummary): void {
    if (!confirm(`Remplacer la clé de « ${sensor.deviceCode} » ? L'ancienne cessera immédiatement.`)) {
      return;
    }
    this.http.post<ProvisionedDevice>(`${this.baseUrl}${API_PATHS.devices.rotateSensorKey(sensor.deviceId)}`, {})
      .subscribe({
        next: (device) => { this.revealedKey = { deviceCode: device.deviceCode, apiKey: device.apiKey }; },
        error: () => errorAlert('Échec de la rotation de clé.')
      });
  }

  rotateTrackerKey(tracker: DeviceSummary): void {
    if (!confirm(`Remplacer la clé de « ${tracker.deviceCode} » ? L'ancienne cessera immédiatement.`)) {
      return;
    }
    this.http.post<ProvisionedDevice>(`${this.baseUrl}${API_PATHS.devices.rotateVehicleTrackerKey(tracker.deviceId)}`, {})
      .subscribe({
        next: (device) => { this.revealedKey = { deviceCode: device.deviceCode, apiKey: device.apiKey }; },
        error: () => errorAlert('Échec de la rotation de clé.')
      });
  }

  deactivateSensor(sensor: DeviceSummary): void {
    if (!confirm(`Désactiver le capteur « ${sensor.deviceCode} » ?`)) {
      return;
    }
    this.http.delete(`${this.baseUrl}${API_PATHS.devices.deactivateSensor(sensor.deviceId)}`)
      .subscribe({ next: () => this.loadSensors(), error: () => errorAlert('Échec de la désactivation.') });
  }

  deactivateTracker(tracker: DeviceSummary): void {
    if (!confirm(`Désactiver le traceur « ${tracker.deviceCode} » ?`)) {
      return;
    }
    this.http.delete(`${this.baseUrl}${API_PATHS.devices.deactivateVehicleTracker(tracker.deviceId)}`)
      .subscribe({ next: () => this.loadTrackers(), error: () => errorAlert('Échec de la désactivation.') });
  }

}
