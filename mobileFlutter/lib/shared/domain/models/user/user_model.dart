import 'package:equatable/equatable.dart';

class User extends Equatable {
  final int id;
  final String username;
  final String password;
  final String userEmail;
  final String firstName;
  final String lastName;
  final String gender;
  final String image;
  final String token;
  const User({
    this.id = 0,
    this.username = '',
    this.password = '',
    this.userEmail = '',
    this.firstName = '',
    this.lastName = '',
    this.gender = '',
    this.image = '',
    this.token = '',
  });

  @override
  List<Object?> get props => [
        id,
        username,
        password,
        userEmail,
        firstName,
        lastName,
        gender,
        image,
        token,
      ];

  get email => null;

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'id': id,
      'username': username,
      'password': password,
      'userEmail': userEmail,
      'firstName': firstName,
      'lastName': lastName,
      'gender': gender,
      'image': image,
      'token': token,
    };
  }

  factory User.fromJson(Map<String, dynamic> map) => User(
        id: map['id'] ?? 0,
        username: map['username'] ?? '',
        password: map['password'] ?? '',
        userEmail: map['userEmail'] ?? '',
        firstName: map['firstName'] ?? '',
        lastName: map['lastName'] ?? '',
        gender: map['gender'] ?? '',
        image: map['image'] ?? '',
        token: map['token'] ?? '',
      );

  User copyWith({
    int? id,
    String? username,
    String? password,
    String? userEmail,
    String? firstName,
    String? lastName,
    String? gender,
    String? image,
    String? token,
  }) {
    return User(
      id: id ?? this.id,
      username: username ?? this.username,
      password: password ?? this.password,
      userEmail: userEmail ?? this.userEmail,
      firstName: firstName ?? this.firstName,
      lastName: lastName ?? this.lastName,
      gender: gender ?? this.gender,
      image: image ?? this.image,
      token: token ?? this.token,
    );
  }
}
