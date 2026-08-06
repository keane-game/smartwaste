import 'package:dio/dio.dart';

class StatusCode {
  static const int ok = 200;
  static const int created = 201;
  static const int accepted = 202;
  static const int noContent = 204;

  static const int badRequest = 400;
  static const int unauthorized = 401;
  static const int forbidden = 403;
  static const int notFound = 404;
  static const int methodNotAllowed = 405;
  static const int conflict = 409;

  static const int internalServerError = 500;
  static const int notImplemented = 501;
  static const int badGateway = 502;
  static const int serviceUnavailable = 503;
  static const int gatewayTimeout = 504;

  static const int unknown = 520;
}

// Helper method to handle different types of Dio exceptions
String handleDioError(DioException error) {
  switch (error.type) {
    case DioExceptionType.connectionTimeout:
    case DioExceptionType.sendTimeout:
    case DioExceptionType.receiveTimeout:
      return "Timeout occurred while sending or receiving";
    case DioExceptionType.badResponse:
      final statusCode = error.response?.statusCode;
      if (statusCode != null) {
        switch (statusCode) {
          case StatusCode.badRequest:
            return "Bad Request";
          case StatusCode.unauthorized:
          case StatusCode.forbidden:
            return "Unauthorized";
          case StatusCode.notFound:
            return "Not Found";
          case StatusCode.conflict:
            return 'Conflict';

          case StatusCode.internalServerError:
            return "Internal Server Error";
        }
      }
      break;
    case DioExceptionType.cancel:
      break;
    case DioExceptionType.unknown:
      return "No Internet Connection";
    case DioExceptionType.badCertificate:
      return "Internal Server Error";
    case DioExceptionType.connectionError:
      return "Connection Error";
    default:
      return "Unknown Error";
  }
  return "Unknown Error";
}
