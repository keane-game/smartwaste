import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/features/auth/domain/providers/auth_provider.dart';
import 'package:sonaged/features/auth/domain/repository/auth_repository.dart';
import 'package:sonaged/features/auth/presentation/providers/state/auth_notifier.dart';
import 'package:sonaged/features/auth/presentation/providers/state/auth_state.dart';
import 'package:sonaged/services/user_cache_service/domain/providers/user_cache_provider.dart';
import 'package:sonaged/services/user_cache_service/domain/repository/user_cache_repository.dart';

final authStateNotifierProvider =
    StateNotifierProvider<AuthNotifier, AuthState>(
  (ref) {
    final AuthRepository authRepository = ref.watch(authRepositoryProvider);
    final UserRepository userRepository =
        ref.watch(userLocalRepositoryProvider);
    return AuthNotifier(
      authRepository: authRepository,
      userRepository: userRepository,
    );
  },
);
