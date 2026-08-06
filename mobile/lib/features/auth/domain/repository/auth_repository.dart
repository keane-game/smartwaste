import 'package:sonaged/shared/domain/models/either.dart';
import 'package:sonaged/shared/domain/models/user/user_model.dart';
import 'package:sonaged/shared/exceptions/http_exception.dart';

abstract class AuthRepository {
  Future<Either<AppException, User>> loginUser({required User user});
}
