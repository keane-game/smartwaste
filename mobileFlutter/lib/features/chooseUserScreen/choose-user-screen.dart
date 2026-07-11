import 'package:flutter/material.dart';
import 'package:sonaged/features/liveTrackigUtils/maps_host.dart';
import 'package:sonaged/features/liveTrackigUtils/choose_device.dart';
import 'package:sonaged/features/liveTrackigUtils/coordinates_model.dart';

class ChooseUser extends StatelessWidget {
  late CoordinatesModel coordinatesModel;

  ChooseUser({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Google Maps Flutter')),
      backgroundColor: Colors.black,
      body: Container(
        decoration: const BoxDecoration(
          image: DecorationImage(
            image: AssetImage("assets/images/pexels3.jpg"),
            fit: BoxFit.cover,
          ),
        ),
        child: Builder(
          builder: (context) => Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                SizedBox(
                  width: double.infinity,
                  height: 55,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(15)),
                      backgroundColor: const Color(0xDB5D8B47),
                      fixedSize: const Size(300, 65),
                    ),
                    child: const Text('Share my realtime location'),
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (context) => const MapsHost()),
                      );
                    },
                  ),
                ),
                const Padding(
                  padding: EdgeInsets.all(8.0),
                ),
                SizedBox(
                    width: double.infinity,
                    height: 55,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(15)),
                        backgroundColor: const Color(0xDB5D8B47),
                        fixedSize: const Size(300, 65),
                      ),
                      child: const Text('Get realtime location'),
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (context) => const ChooseDevice()),
                        );
                      },
                    ))
              ],
            ),
          ),
        ),
      ),
    );
  }
}
