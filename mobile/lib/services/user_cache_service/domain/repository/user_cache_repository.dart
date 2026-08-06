import 'package:sonaged/shared/domain/models/either.dart';
import 'package:sonaged/shared/domain/models/models.dart';
import 'package:sonaged/shared/exceptions/http_exception.dart';

abstract class UserRepository {
  Future<Either<AppException, User>> fetchUser();
  Future<bool> saveUser({required User user});
  Future<bool> deleteUser();
  Future<bool> hasUser();
  Future<bool> saveBearer({required String bearer});
}
