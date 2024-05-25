import 'dart:async';

import 'package:flutter/material.dart';
import 'package:sonaged/features/dashboard/presentation/providers/dashboard_state_provider.dart';
import 'package:sonaged/features/dashboard/presentation/providers/state/dashboard_state.dart';
import 'package:sonaged/features/dashboard/presentation/widgets/dashboard_drawer.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/shared/widgets/app_shadow.dart';
import 'package:sonaged/shared/widgets/bottom_navbar_items.dart';

class DashboardScreen extends ConsumerStatefulWidget {
  static const String routeName = 'DashboardScreen';

  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  final scrollController = ScrollController();
  final TextEditingController searchController = TextEditingController();
  bool isSearchActive = false;
  Timer? _debounce;

  @override
  void initState() {
    super.initState();
    scrollController.addListener(scrollControllerListener);
  }

  @override
  void dispose() {
    _debounce?.cancel();
    super.dispose();
  }

  void scrollControllerListener() {
    if (scrollController.position.maxScrollExtent == scrollController.offset) {
      final notifier = ref.read(dashboardNotifierProvider.notifier);
      if (isSearchActive) {
        notifier.searchProducts(searchController.text);
      } else {
        notifier.fetchProducts();
      }
    }
  }

  void refreshScrollControllerListener() {
    scrollController.removeListener(scrollControllerListener);
    scrollController.addListener(scrollControllerListener);
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
        appBar: AppBar(
          title: isSearchActive
              ? TextField(
                  style: Theme.of(context).textTheme.bodyMedium,
                  decoration: InputDecoration(
                    hintText: 'Rechercher',
                    hintStyle: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onBackground,
                        ),
                    focusedBorder: UnderlineInputBorder(
                      borderSide: BorderSide(
                        color: Theme.of(context).colorScheme.onBackground,
                      ),
                    ),
                    border: UnderlineInputBorder(
                      borderSide: BorderSide(
                        color: Theme.of(context).colorScheme.onBackground,
                      ),
                    ),
                  ),
                  controller: searchController,
                  onChanged: _onSearchChanged,
                )
              : const Text('Dashboard'),
          actions: [
            IconButton(
              onPressed: () {
                searchController.clear();
                setState(() {
                  isSearchActive = !isSearchActive;
                });

                ref.read(dashboardNotifierProvider.notifier).resetState();
                if (!isSearchActive) {
                  ref.read(dashboardNotifierProvider.notifier).fetchProducts();
                }
                refreshScrollControllerListener();
              },
              icon: Icon(
                isSearchActive ? Icons.clear : Icons.search,
              ),
            ),
          ],
        ),
        drawer: const DashboardDrawer(),
        body: const Text("test"),
        bottomNavigationBar: Container(
          width: 375,
          height: 58,
          decoration: appBoxShadowWithRaduis(),
          child: BottomNavigationBar(elevation: 0, items: bottomNavbarItems),
        ),
      ),
    );
  }

  _onSearchChanged(String query) {
    if (_debounce?.isActive ?? false) _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 500), () {
      ref.read(dashboardNotifierProvider.notifier).searchProducts(query);
    });
  }
}
