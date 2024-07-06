
import L from 'leaflet';
import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Map, map, tileLayer } from 'leaflet';
@Component({
  selector: 'app-maps',
  templateUrl: './maps.component.html',
  styleUrls: ['./maps.component.scss']
})
export class MapsComponent implements OnInit {


  @ViewChild('map')
  mapElementRef: ElementRef = null!;

  private _map: Map = null!;

  private map!: L.Map
  markers: L.Marker[] = [
    L.marker([31.9539, 35.9106]), // Amman
    L.marker([32.5568, 35.8469]) // Irbid
  ];

  constructor() { }

  ngOnInit(): void {
    //this.initMap();
  }

  ngAfterViewInit(): void {

   
    this.initMap();
 
  }



  private initMap(): void {
    const map = L.map(this.mapElementRef.nativeElement).setView([51.505, -0.09], 13); // Initial coordinates and zoom level

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
    }).addTo(map);

    // Example markers (replace with your trash locations)
    const trashLocations = [
      { lat: 51.5, lng: -0.09 },
      { lat: 51.51, lng: -0.1 },
      { lat: 51.49, lng: -0.1 }
    ];

    trashLocations.forEach(location => {
      L.marker([location.lat, location.lng]).addTo(map)
        .bindPopup('Trash location') // Popup content
        .openPopup();
    });
  }



  private initializeMap() {

    const baseMapURl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
    this._map = map(this.mapElementRef.nativeElement)
    .setView([46.801111, 8.226667], 8);

   // this.map = L.map('map');
    L.tileLayer(baseMapURl).addTo(this._map);
  }


  private addMarkers() {
    // Add your markers to the map
    this.markers.forEach(marker => marker.addTo(this._map));
  }

  private centerMap() {
    // Create a LatLngBounds object to encompass all the marker locations
    const bounds = L.latLngBounds(this.markers.map(marker => marker.getLatLng()));
    
    // Fit the map view to the bounds
    this._map.fitBounds(bounds);
  }

}
