import 'package:equatable/equatable.dart';

class AuthArgs extends Equatable {
  final String username;
  final String password;
  AuthArgs({required this.username, required this.password});

  @override
  List<Object?> get props => [
        username,
        password,
      ];

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'username': username,
      'password': password,
    };
  }
}

class AuthResponse {
  AuthResponse({required this.token, required this.statusCode});

  final String token;
  final int statusCode;

  AuthResponse.fromJson(Map<String, dynamic> json)
      : token = json['token'],
        statusCode = json['statusCode'];
}
