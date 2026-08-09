import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import * as L from 'leaflet';
import 'esri-leaflet';
import { MapsService, DepotoirMap } from '../../../services/maps.service';

@Component({
    selector: 'app-maps',
    templateUrl: './maps.component.html',
    styleUrls: ['./maps.component.scss'],
    standalone: false
})
export class MapsComponent implements OnInit, AfterViewInit {

  @ViewChild('map', { static: false }) mapElementRef: ElementRef = null!;
  private map: L.Map = null!;
  senegalCoords: L.LatLngTuple = [14.4974, -14.4524];
  departPikineCoords: L.LatLngTuple = [14.7739, -17.3684];
  iconBacs = "../../assets/images/bacs.png";
  iconPP =  "../../assets/images/iconclean.png";
  iconPrn =  "../../assets/images/iconprn.png";
  mapUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';

  customIconPP = L.icon({
    iconUrl: this.iconPP,
    iconSize: [28, 28], // size of the icon
  });

  customIconBacs = L.icon({
    iconUrl: this.iconBacs,
    iconSize: [28, 28], // size of the icon
  });
  customIconPrn = L.icon({
    iconUrl: this.iconPrn,
    iconSize: [28, 28], // size of the icon
  });


  constructor(
    private mapsService: MapsService,) {
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.addPolygon();
    this.addMarkers();
  }

  private initMap(): void {
    // Initialize the map
    this.map = L.map(this.mapElementRef.nativeElement, {
      center: this.departPikineCoords,
      zoom: 13
    }).setView([...this.departPikineCoords]);

    // Add OpenStreetMap tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
  }

  private addPolygon(): void {
    // Les coordonnées `/v1/maps/departments` sont en WGS84 (ADR-0015), en chaînes — `toLatLng()`
    // fait le `parseFloat`, aucune projection à appliquer.
    this.mapsService.getDepartment().subscribe(department => {
      const points = MapsService.toLatLng(department.coordinates);
      if (points.length === 0) {
        return;
      }
      L.polygon([points], {
        color: 'red',
        fillColor: '#f03',
        fillOpacity: 0.5
      }).addTo(this.map);
    });
  }

  private addMarkers(): void {
    this.mapsService.getDepotoirs().subscribe(depotoirs => {
      depotoirs.forEach((d: DepotoirMap) => {
        let icon = this.customIconBacs;
        if (d.typeDepot === 'PP') {
          icon = this.customIconPP;
        } else if (d.typeDepot === 'PRN') {
          icon = this.customIconPrn;
        }

        const points = MapsService.toLatLng(d.coordinates);
        if (points.length === 0) {
          return;
        }
        L.marker(points[0], { icon })
          .addTo(this.map)
          .bindPopup(`Address: ${d.address} <br> Type: ${d.typeDepot}`);
      });
    });
  }

}
