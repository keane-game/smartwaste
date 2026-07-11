import 'package:sonaged/features/auth/data/datasources/remote/auth_remote_data.dart';
import 'package:sonaged/features/auth/domain/repository/auth_repository.dart';
import 'package:sonaged/shared/domain/models/either.dart';
import 'package:sonaged/shared/domain/models/user/user_model.dart';
import 'package:sonaged/shared/exceptions/http_exception.dart';

class AuthRepositoryImpl extends AuthRepository {
  final LoginUserDataSource dataSource;

  AuthRepositoryImpl(this.dataSource);

  @override
  Future<Either<AppException, User>> loginUser({required User user}) {
    return dataSource.loginUser(user: user);
  }
}
