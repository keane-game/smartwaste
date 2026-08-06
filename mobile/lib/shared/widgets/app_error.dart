import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/shared/theme/app_theme.dart';

class AppError extends ConsumerWidget {
  const AppError({super.key});
  static const String routeName = 'appError';

  @override
  Widget build(BuildContext context, ref) {
    final theme = ref.watch(appThemeProvider.notifier);
    return Scaffold(
      appBar: AppBar(),
      body: InkWell(
        onTap: () {
          theme.toggleTheme();
        },
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error,
              size: 42.0,
              color: Theme.of(context).colorScheme.error,
            ),
            Center(
              child: Text(
                'Error occurred',
                style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      color: Theme.of(context).colorScheme.error,
                    ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
