import 'dart:async';
import 'dart:developer';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:sonaged/shared/data/remote/remote.dart';
import 'package:sonaged/shared/domain/models/either.dart';
import 'package:sonaged/shared/domain/models/response.dart' as response;
import 'package:sonaged/shared/exceptions/http_exception.dart';

mixin ExceptionHandlerMixin on NetworkService {
  Future<Either<AppException, response.Response>>
      handleException<T extends Object>(
          Future<Response<dynamic>> Function() handler,
          {String endpoint = ''}) async {
    try {
      final res = await handler();
      return Right(
        response.Response(
          statusCode: res.statusCode ?? 200,
          data: res.data,
          statusMessage: res.statusMessage,
        ),
      );
    } catch (e) {
      String message = '';
      String identifier = '';
      int statusCode = 0;
      log(e.runtimeType.toString());
      switch (e) {
        case SocketException _:
          message = 'Impossible de se connecter au serveur.';
          statusCode = 0;
          identifier = 'Socket Exception ${e.message}\n at  $endpoint';
          break;

        case DioException _:
          message = e.response?.data?['message'] ?? 'Internal Error occurred';
          statusCode = e.response?.statusCode ?? 500;
          identifier = 'DioException ${e.message} \nat  $endpoint';
          break;

        default:
          message = 'Une erreur inconnue s\'est produite';
          statusCode = 2;
          identifier = 'Unknown error ${e.toString()}\n at $endpoint';
      }

      return Left(
        AppException(
          message: message,
          statusCode: statusCode,
          identifier: identifier,
        ),
      );
    }
  }
}
