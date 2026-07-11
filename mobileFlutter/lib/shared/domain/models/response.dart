import 'package:sonaged/shared/domain/models/either.dart';
import 'package:sonaged/shared/exceptions/http_exception.dart';

class Response {
  final int statusCode;
  final String? statusMessage;
  final dynamic data;

  Response({required this.statusCode, this.statusMessage, this.data});
  @override
  String toString() {
    return 'statusCode=$statusCode\nstatusMessage=$statusMessage\n data=$data';
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'statusCode': statusCode,
      'statusMessage': statusMessage,
      'data': data['bearer'],
    };
  }
}

extension ResponseExtension on Response {
  Right<AppException, Response> get toRight => Right(this);
}
