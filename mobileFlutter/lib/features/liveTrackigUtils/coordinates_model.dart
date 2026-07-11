import 'package:firebase_database/firebase_database.dart';

class CoordinatesModel {
  String? id;
  double? latitude;
  double? longitude;

  CoordinatesModel(this.latitude, this.longitude);

  CoordinatesModel.map(dynamic obj) {
    id = obj['id'];
    latitude = obj['latitude'];
    longitude = obj['longitude'];
  }

  double? get currentLatitude => latitude;
  double? get currentLongitude => longitude;

  CoordinatesModel.fromSnapshot(DataSnapshot snapshot) {
    dynamic values = snapshot.value;
    id = snapshot.key;
    latitude = values['latitude'];
    longitude = values['longitude'];
  }
}
