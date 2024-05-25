import 'package:jwt_decoder/jwt_decoder.dart';
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
          /// GET RESPONSE TO JSON (TOKEN)
          Map<String, dynamic> token = response.toJson();

          /// DECOCETOKEN
          Map<String, dynamic> decodeToken = JwtDecoder.decode(token['data']);

          decodeToken['token'] = token['data'];
          final user = User.fromJson(decodeToken);
          //print('data : $user');
          //print('data : $decodeToken');
          networkService.updateHeader(
            {'Authorization': 'bearer ${token['data']}'},
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
