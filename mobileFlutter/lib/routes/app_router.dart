import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:sonaged/features/auth/presentation/screens/login_screen.dart';
import 'package:sonaged/features/auth/presentation/screens/register.dart';
import 'package:sonaged/features/welcome/presentation/screens/splash_screen.dart';

import '../features/welcome/presentation/screens/welcome_screen.dart';

/// Main router for the Example app
///
/// ! Pay attention to the order of routes.
/// Create:  example/create
/// View:    example/:eid
/// Edit:    example/:eid/edit
/// where :edit means example entity id.
///
/// ! Note about parameters
/// Router keeps parameters in global map. It means that if you create route
/// organization/:id and organization/:id/department/:id. Department id will
///  override organization id. So use :oid and :did instead of :id
/// Also router does not provide option to set regex for parameters.
/// If you put - example/:eid before - example/create for route - example/create
/// will be called route - example/:eid
///
///

part 'app_router.g.dart';

enum Routes {
  splash,
  welcome,
  login,
  register,
}

final _rootNavigatorKey = GlobalKey<NavigatorState>();

@Riverpod(keepAlive: true)
GoRouter goRouter(GoRouterRef ref) {
  return GoRouter(
    initialLocation: '/',
    navigatorKey: _rootNavigatorKey,
    debugLogDiagnostics: true,
    routes: [
      GoRoute(
        path: '/',
        name: Routes.splash.name,
        builder: (context, state) => SplashScreen(key: state.pageKey),
      ),
      GoRoute(
        path: '/welcome',
        name: Routes.welcome.name,
        builder: (context, state) => WelcomeScreen(key: state.pageKey),
      ),
      GoRoute(
        path: '/login',
        name: Routes.login.name,
        builder: (context, state) => LoginScreen(key: state.pageKey),
      ),
      GoRoute(
        path: '/register',
        name: Routes.register.name,
        builder: (context, state) => RegisterPage(key: state.pageKey),
      ),
    ],
  );
}

// /// Route observer to use with RouteAware
// final RouteObserver<ModalRoute<void>> routeObserver =
//     RouteObserver<ModalRoute<void>>();
