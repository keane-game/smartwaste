import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/features/dashboard/presentation/providers/dashboard_state_provider.dart';
import 'package:sonaged/features/dashboard/presentation/providers/state/dashboard_state.dart';
import 'package:sonaged/features/dashboard/presentation/widgets/dashboard_drawer.dart';
import 'package:sonaged/features/welcome/presentation/screens/welcome_screen.dart';
import 'package:sonaged/shared/widgets/app_shadow.dart';
import 'package:sonaged/shared/widgets/bottom_navbar_items.dart';

class DashboardScreen extends ConsumerStatefulWidget {
  static const String routeName = 'DashboardScreen';

  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  int _selectedIndex = 0;
  bool isSearchActive = false;
  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(dashboardNotifierProvider);

    ref.listen(
      dashboardNotifierProvider.select((value) => value),
      ((DashboardState? previous, DashboardState next) {
        //show Snackbar on failure
        if (next.state == DashboardConcreteState.fetchedAllProducts) {
          if (next.message.isNotEmpty) {
            ScaffoldMessenger.of(context)
                .showSnackBar(SnackBar(content: Text(next.message.toString())));
          }
        }
      }),
    );
    return SafeArea(
      child: Scaffold(
        body: Center(
          child: _buildPage(_selectedIndex),
        ),
        appBar: AppBar(
          title: isSearchActive
              ? TextField(
                  style: Theme.of(context).textTheme.bodyMedium,
                  decoration: InputDecoration(
                    hintText: 'Rechercher',
                    hintStyle: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurface,
                        ),
                    focusedBorder: UnderlineInputBorder(
                      borderSide: BorderSide(
                        color: Theme.of(context).colorScheme.onSurface,
                      ),
                    ),
                    border: UnderlineInputBorder(
                      borderSide: BorderSide(
                        color: Theme.of(context).colorScheme.onSurface,
                      ),
                    ),
                  ),
                )
              : const Text('Dashboard'),
        ),
        drawer: const DashboardDrawer(),
        bottomNavigationBar: Container(
          width: 375,
          height: 58,
          decoration: appBoxShadowWithRaduis(),
          child: BottomNavigationBar(
            items: BottomNavBarItems.items,
            currentIndex: _selectedIndex,
            selectedItemColor: Colors.amber[800],
            onTap: _onItemTapped,
          ),
        ),
      ),
    );
  }
}

Widget _buildPage(int index) {
  switch (index) {
    case 0:
      return WelcomeScreen();
    case 1:
    default:
      return WelcomeScreen();
  }
}
