import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/features/auth/data/datasources/remote/auth_remote_data.dart';
import 'package:sonaged/features/auth/data/repository/auth_repository_impl.dart';
import 'package:sonaged/features/auth/domain/repository/auth_repository.dart';
import 'package:sonaged/shared/data/remote/network_service.dart';
import 'package:sonaged/shared/domain/providers/dio_network_service_provider.dart';

final authDataSourceProvider =
    Provider.family<LoginUserDataSource, NetworkService>(
  (_, networkService) => LoginUserRemoteDataSource(networkService),
);

final authRepositoryProvider = Provider<AuthRepository>(
  (ref) {
    final NetworkService networkService = ref.watch(networkServiceProvider);
    final LoginUserDataSource dataSource =
        ref.watch(authDataSourceProvider(networkService));
    return AuthRepositoryImpl(dataSource);
  },
);
