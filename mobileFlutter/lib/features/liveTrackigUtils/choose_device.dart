import 'dart:async';

import 'package:flutter/material.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:sonaged/features/liveTrackigUtils/maps_receiver.dart';

class ChooseDevice extends StatefulWidget {
  const ChooseDevice({super.key});

  @override
  State createState() => ChooseDeviceState();
}

class ChooseDeviceState extends State<ChooseDevice> {
  static final databaseReference = FirebaseDatabase.instance.reference();

  static double currentLatitude = 0.0;
  static double currentLongitude = 0.0;

  late StreamSubscription subscription;

  Map<String, double> currentLocation = {};
  late StreamSubscription<Map<String, double>> locationSubcription;
  String? error;

  String deviceid = 'Unknown';

  List<String> list = [];

  @override
  void dispose() {
    subscription.cancel();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();
    databaseReference.once().then((DataSnapshot snapshot) {
          Map<dynamic, dynamic>? values = snapshot.value as Map?;
          values?.forEach((key, values) {
            setState(() {
              list.add(key);
            });
          });
        } as FutureOr Function(DatabaseEvent value));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(title: const Text('Choose device to track')),
        body: ListView.builder(
            itemBuilder: (BuildContext context, int index) {
              return GestureDetector(
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (context) =>
                            MapsReceiver(deviceid: list[index])),
                  );
                },
                child: Card(
                  child: SizedBox(
                    height: 50,
                    width: 240,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(15)),
                        backgroundColor: const Color(0xDB5D8B47),
                        fixedSize: const Size(300, 65),
                      ),
                      onPressed: () {},
                      child: Text('Device ID : ${list[index]}'),
                    ),
                  ),
                ),
              );
            },
            itemCount: list.length));
  }
}
