// ignore_for_file: unused_import

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/core/app.dart';
import 'package:sonaged/core/app_env.dart';
import 'package:sonaged/core/observers.dart';

void main() => mainCommon(AppEnvironment.PROD);

Future<void> mainCommon(AppEnvironment environment) async {
  WidgetsFlutterBinding.ensureInitialized();
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
