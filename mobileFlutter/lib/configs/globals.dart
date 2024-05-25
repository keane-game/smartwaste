import 'dart:io';

final kTestMode = Platform.environment.containsKey('FLUTTER_TEST');
// ignore: constant_identifier_names
const int PRODUCTS_PER_PAGE = 20;
// ignore: constant_identifier_names
const String USER_LOCAL_STORAGE_KEY = 'user';
// ignore: constant_identifier_names
const String APP_THEME_STORAGE_KEY = 'AppTheme';
// ignore: non_constant_identifier_names
String IS_AUTHENTICATED_KEY = 'IS_AUTHENTICATED_KEY';
// ignore:  non_constant_identifier_names
String AUTHENTICATED_USER_EMAIL_KEY = 'AUTHENTICATED_USER_EMAIL_KEY';

const String TOKEN_KEY = "tokenKey";
