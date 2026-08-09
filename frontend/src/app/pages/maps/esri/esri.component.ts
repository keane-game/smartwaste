
import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import * as L from 'leaflet';
import 'esri-leaflet';
import proj4 from 'proj4';
import 'proj4leaflet';
import { MapsService } from '../../../services/maps.service';

import { Feature, Polygon } from 'geojson';
import { departmantjson } from '../../../services/departmentLimit'; // Adjust the path as necessary
import { headerTitleService } from '../../../services/headerTitle.service';


@Component({
    selector: 'app-esri',
    templateUrl: './esri.component.html',
    styleUrl: './esri.component.scss',
    standalone: false
})
export class EsriComponent implements OnInit, AfterViewInit {


@ViewChild('map', { static: false }) mapElementRef: ElementRef = null!;
private map: L.Map = null!;
markers: L.Marker[] = [
  L.marker([31.9539, 35.9106]), // Amman
  L.marker([32.5568, 35.8469]) // Irbid
];
iconBacs = "../../assets/images/bacs.png"
mapUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';

depotoirs: any[] = [];
department: any[] = [];
totalDepotoirs: any;
  private polygon!: L.Polygon;
  private customProjection: string = '+proj=utm +zone=28 +datum=WGS84 +units=m +no_defs';
  private crs: L.CRS;
  senegalCoords: L.LatLngTuple = [14.4974, -14.4524];
  departPikineCoords: L.LatLngTuple = [14.7739, -17.3684];

  customIcon = L.icon({
    iconUrl: this.iconBacs,
    iconSize: [28, 28], // size of the icon
  });


// Example markers (replace with your trash locations)
trashLocations = [
  { lat: 51.5, lng: -0.09 },
  { lat: 51.51, lng: -0.1 },
  { lat: 51.49, lng: -0.1 },
];

departmentGeoJSON: Feature<Polygon> = {
  type: 'Feature',
  geometry: {
    type: 'Polygon',
    coordinates: []
  },
  properties: {
    name: 'PIKINE'
  }
};

constructor(
  private mapsService: MapsService,
  private headerTitleService: headerTitleService
) {

  // Define your custom projection
  proj4.defs('EPSG:32628', this.customProjection);

  // Create Leaflet CRS using the custom projection
  this.crs = new L.Proj.CRS('EPSG:32628', this.customProjection);
 }

ngOnInit(): void {
  this.headerTitleService.setTitle('Carte');
}

getDepotoirs(): any {
  this.mapsService.url = '/depotoirs';
  this.mapsService.getAll().subscribe((resp) => {
    this.depotoirs = resp;
    //this.transformToGeoJSONs( this.depotoirs);
  // console.log(resp);
  this.addMarkers(resp);
  });
}

getDepartment(): void {
  this.mapsService.url = '/departments';
  this.mapsService.getAll().subscribe((resp: any) => {
    this.department = resp;
    this.transformToGeoJSON(this.department);
   // console.log(resp);
  });
}
states: Feature<Polygon> = {
  "type": "Feature",
  "properties": {"party": "Republican"},
  "geometry": {
      "type": "Polygon",
      "coordinates": [

        [
  [1630554.6491661742, 246861.98134764843],
 [1630629.2618154027, 246586.1526709944],
 [1630668.155643193, 246414.7023280915],
 [1630674.8585060704, 246378.8912284961],
 [1630724.9088816904, 246111.4892216688],
 [1630731.154113412, 246077.93150990922],
 [1630731.303906098, 246077.12662387267],
 [1630754.6745662317, 245951.54827678297],
 [1630784.8371265512, 245777.31980333105],
 [1630797.934082033, 245699.13531494793],
[1630818.9685058668, 245537.60687255953],
[1630554.6491661742, 246861.98134764843]
      ]]
  }
};


transformToGeoJSONs(data: any): void {
  console.log(data);
  let dataCoordinates = data.coordinates;
  const coordinates = dataCoordinates.map((coord: { longitude: string; latitude: string; }) => [
    parseFloat(coord.longitude),
    parseFloat(coord.latitude)
  ]);
  this.departmentGeoJSON.geometry.coordinates = [coordinates];
  this.departmentGeoJSON.properties = { name: data.name };
}


transformToGeoJSON(data: any): void {
  let dataCoordinates = data.coordinates;
  console.log(dataCoordinates);
  if ( dataCoordinates!.length > 0) {
    const coordinates = dataCoordinates.map((coord: { longitude: string; latitude: string; }) => [
      parseFloat(coord.longitude),
      parseFloat(coord.latitude)
    ]);
    this.departmentGeoJSON.geometry.coordinates = [...coordinates];
  } else {
    console.error('Department data is not an array or is empty');
  }
}

ngAfterViewInit(): void {

//  this.getDepartment();
//  this.getDepotoirs();
  this.initMap();
}

private initMap(): void {
  const zoomLevel = 8; // Adjust the zoom level as needed

  this.map = L.map(this.mapElementRef.nativeElement).setView(this.senegalCoords, zoomLevel);

  L.tileLayer(this.mapUrl, {
    attribution: '© OpenStreetMap'
  }).addTo(this.map);

  this.trashLocations.forEach(location => {
    L.marker([location.lat, location.lng], { icon: this.customIcon })
      .addTo(this.map)
      .bindPopup('Trash location')
      .openPopup();
  });
  

 // L.geoJSON(this.states).addTo(this.map);

  this.getDepotoirs();
  // console.log(this);
  // console.log(this.departmentGeoJSON);
}

private addMarker() {
  // Add your markers to the map
  this.markers.forEach(marker => marker.addTo(this.map));
}

private centerMap() {
  // Create a LatLngBounds object to encompass all the marker locations
  const bounds = L.latLngBounds(this.markers.map(marker => marker.getLatLng()));
  
  // Fit the map view to the bounds
  this.map.fitBounds(bounds);
}


private addMarkers(data : any): void {
  if (Array.isArray(data) && data.length > 0) {

    data.forEach((depotoir: { coordinates: { latitude: string; longitude: string; }[]; address: any; }) => {
      depotoir.coordinates.forEach((coord: { latitude: string; longitude: string; }) => {
        const lat = parseFloat(coord.latitude);
        const lng = parseFloat(coord.longitude);
        console.log(lat, lng);
      
        if (!isNaN(lat) && !isNaN(lng)) {
          L.marker([lat, lng], { icon: this.customIcon })
            .addTo(this.map)
            .bindPopup(`Address: ${depotoir.address}`);
        }
      });
    });
  } else {
    console.error('Depotoirs data is not an array or is empty');
  }
}
}