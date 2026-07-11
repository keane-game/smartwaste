import 'package:sonaged/services/user_cache_service/data/datasource/user_local_datasource.dart';
import 'package:sonaged/services/user_cache_service/data/repository/user_repository_impl.dart';
import 'package:sonaged/services/user_cache_service/domain/repository/user_cache_repository.dart';
import 'package:sonaged/shared/data/local/storage_service.dart';
import 'package:sonaged/shared/domain/providers/shared_preferences_storage_service_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final userDatasourceProvider = Provider.family<UserDataSource, StorageService>(
  (_, networkService) => UserLocalDatasource(networkService),
);

final userLocalRepositoryProvider = Provider<UserRepository>((ref) {
  final storageService = ref.watch(storageServiceProvider);

  final datasource = ref.watch(userDatasourceProvider(storageService));

  final repository = UserRepositoryImpl(datasource);

  return repository;
});
