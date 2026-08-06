import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import * as L from 'leaflet';
import 'esri-leaflet';
import proj4 from 'proj4';
import 'proj4leaflet';
import { MapsService } from '../../../services/maps.service';

import { departmantjson } from '../../../services/departmentLimit'; // Adjust the path as necessary


@Component({
  selector: 'app-maps',
  templateUrl: './maps.component.html',
  styleUrls: ['./maps.component.scss']
})
export class MapsComponent implements OnInit, AfterViewInit {

 
  @ViewChild('map', { static: false }) mapElementRef: ElementRef = null!;
  private map: L.Map = null!;
  private polygon!: L.Polygon;
  private customProjection: string = '+proj=utm +zone=28 +datum=WGS84 +units=m +no_defs';
  private crs: L.CRS;
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
    // Define your custom projection
    proj4.defs('EPSG:32628', this.customProjection);

    // Create Leaflet CRS using the custom projection
    this.crs = new L.Proj.CRS('EPSG:32628', this.customProjection);
  }

  ngOnInit(): void {


    //  // Example conversion usage
    //   const x = 246861.98134764843;
    //   const y = 1630554.6491661742;
    //   const convertedCoords = this.convertCoordinates(x, y);
    //   console.log('Converted convertedCoords:', convertedCoords);

    //   let coordinat= this.coordinates.map(coord => [
    //     this.convertCoordinates(coord[0], coord[1])
    //   ]);
    //   console.log('Converted coordinat:', coordinat);
    //   const coordinates = this.geoJsonData.coordinates.map((coord:any) => [
    //     this.convertCoordinates(coord.latitude, coord.longitude)
    //   ]);
    //   console.log('Converted coordinates:', coordinates);
  }

  ngAfterViewInit(): void {
    this.initMap();
    //this.getDepartment();
    this.addPolygon();
    this. addMarkers();
    //this.getCurrentLocation()


  }

  getDepotoirs(): any {
    this.mapsService.url = '/depotoirs';
    this.mapsService.getAll().subscribe((resp) => {
      //this.transformToGeoJSONs( this.depotoirs);
      // console.log(resp);
    });
  }

  getDepartment(): void {
    this.mapsService.url = '/departments';
    this.mapsService.getAll().subscribe((resp: any) => {
      console.log(resp);
    });
  }

  private initMap(): void {
    const zoomLevel = 8; // Adjust the zoom level as needed

    // Define the projection for Senegal (EPSG:32628)
    const utm28n = new L.Proj.CRS('EPSG:32628', '+proj=utm +zone=28 +datum=WGS84 +units=m +no_defs', {
      origin: [-4000000.0, 4000000.0],
      resolutions: [
        8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5, 0.25, 0.125, 0.0625, 0.03125, 0.015625
      ]
    });

    // Initialize the map with the defined CRS
    this.map = L.map(this.mapElementRef.nativeElement, {
      // crs: utm28n,// Initial center of the map (Senegal in EPSG:4326)
      center: this.departPikineCoords,
      zoom: 13

    }).setView([...this.departPikineCoords]);

    // Add OpenStreetMap tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);


  }

  private addPolygon(): void {

    const coordinates = departmantjson.coordinates.map((coord: any) => [
      this.convertCoordinates(coord.latitude, coord.longitude)
    ]);
    console.log('Converted coordinates:', coordinates);

    L.polygon([coordinates], {
      color: 'red',
      fillColor: '#f03',
      fillOpacity: 0.5
    }).addTo(this.map);

  }

  private addMarkers(): void {

    this.mapsService.url = '/depotoirs';
    this.mapsService.getAll().subscribe((resp) => {

      resp.forEach((d: any) => {
        let icon = this.customIconBacs;
        if (d.typeDepot == 'PP' ){
          icon = this.customIconPP;
        }else if (d.typeDepot == 'PRN' ){
          icon = this.customIconPrn;
        }

        console.log('Converted coordinates:', d.typeDepot);

        const coordinate = this.convertCoordinates(d.coordinates[0].latitude, d.coordinates[0].longitude);
        if (coordinate) {
          L.marker([...coordinate], { icon })
            .addTo(this.map)
            .bindPopup(`Address: ${d.address} <br> Type: ${d.typeDepot}`);
        }
      });
    });


  }


  private convertToLatLng(x: string, y: string): [number, number] {
    // Define the projection for EPSG:32628

    proj4.defs('EPSG:32628', '+proj=utm +zone=28 +datum=WGS84 +units=m +no_defs');
    proj4.defs('EPSG:4326', '+proj=longlat +datum=WGS84 +no_defs');


    // Convert coordinates from EPSG:32628 to EPSG:4326
    const latlng = proj4('EPSG:32628', 'EPSG:4326', [parseFloat(x), parseFloat(y)]);
    return [latlng[1], latlng[0]]; // Return as [lat, lng]
  }

  convertCoordinates(x: any, y: any): [number, number] {

    proj4.defs('EPSG:4326', '+proj=longlat +datum=WGS84 +no_defs');


    // Convert coordinates from EPSG:32628 to EPSG:4326
    const latlng = proj4('EPSG:32628', 'EPSG:4326', [parseFloat(x), parseFloat(y)]);
    return [latlng[1], latlng[0]]; // Return as [lat, lng]
  }


  private getCurrentLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        position => {
          const coords = position.coords;
          const latLng = L.latLng(coords.latitude, coords.longitude);
  
          this.map.setView(latLng, 13);
  
          L.marker(latLng)
            .addTo(this.map)
            .bindPopup('You are here!')
            .openPopup();
        },
        error => {
          console.error(error);
          alert('Unable to retrieve your location.');
        }
      );
    } else {
      console.error('Geolocation is not supported by this browser.');
      alert('Geolocation is not supported by this browser.');
    }
  }
  

}