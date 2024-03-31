import 'package:sonaged/shared/data/remote/network_service.dart';
import 'package:sonaged/shared/domain/models/either.dart';
import 'package:sonaged/shared/domain/models/user/user_model.dart';
import 'package:sonaged/shared/exceptions/http_exception.dart';

abstract class LoginUserDataSource {
  Future<Either<AppException, User>> loginUser({required User user});
}

class LoginUserRemoteDataSource implements LoginUserDataSource {
  final NetworkService networkService;

  LoginUserRemoteDataSource(this.networkService);

  @override
  Future<Either<AppException, User>> loginUser({required User user}) async {
    try {
      final eitherType = await networkService.post(
        '/connexion',
        data: user.toJson(),
      );
      return eitherType.fold(
        (exception) {
          return Left(exception);
        },
        (response) {
          final user = User.fromJson(response.data);

          // print(response.data.toString());
          // update the token for requests
          networkService.updateHeader(
            {'Authorization': 'bearer ${response.data!.bearer}'},
          );

          return Right(user);
        },
      );
    } catch (e) {
      return Left(
        AppException(
          message: 'Unknown error occurred',
          statusCode: 1,
          identifier: '${e.toString()}\nLoginUserRemoteDataSource.loginUser',
        ),
      );
    }
  }
}
