// ignore_for_file: unused_import

import 'package:firebase_core_dart/firebase_core_dart.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/core/app.dart';
import 'package:sonaged/core/app_env.dart';
import 'package:sonaged/core/observers.dart';

void main() => mainCommon(AppEnvironment.PROD);

Future<void> mainCommon(AppEnvironment environment) async {
  WidgetsFlutterBinding.ensureInitialized();
//   await Firebase.initializeApp(
//       options: const FirebaseOptions(
//     apiKey: 'AIzaSyCubDuko2e-awOHaGbIYUpiAL6smS-93lw',
//     appId: '1:817107836674:android:108bd4886eea4842fcec1b',
//     messagingSenderId: '817107836674',
//     projectId: 'test-256ab',
//   ));
  //runApp(const MaterialApp());
  EnvInfo.initialize(environment);

  // Setting Device Orientation
  // SystemChrome.setPreferredOrientations([
  //   DeviceOrientation.portraitUp,
  //   DeviceOrientation.portraitDown,
  // ]);

//  SystemChrome.setSystemUIOverlayStyle(
//     SystemUiOverlayStyle.light.copyWith(
//       statusBarColor: Colors.black,
//       statusBarBrightness: Brightness.light,
//     ),
//   );

  runApp(ProviderScope(
    observers: [
      Observers(),
    ],
    child: const MyApp(),
  ));
}
