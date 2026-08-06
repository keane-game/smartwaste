import 'package:flutter/material.dart';

void showActionSnackBar(BuildContext context, String msg) {
  final snackBar = SnackBar(
    content: Text(
      msg,
      style: const TextStyle(fontSize: 16),
    ),
    action: SnackBarAction(
      label: 'Click Me',
      onPressed: () => print('Clicked on SnackBar Action!'),
    ),
  );

  ScaffoldMessenger.of(context)
    ..removeCurrentSnackBar()
    ..showSnackBar(snackBar);
}

void showFloatingSnackBar(BuildContext context, String msg) {
  final snackBar = SnackBar(
    content: Text(
      msg,
      style: const TextStyle(fontSize: 24),
      textAlign: TextAlign.center,
    ),
    backgroundColor: Colors.green,
    duration: Duration(seconds: 3),
    shape: StadiumBorder(),
    margin: EdgeInsets.symmetric(vertical: 16, horizontal: 12),
    behavior: SnackBarBehavior.floating,
    elevation: 0,
  );

  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(snackBar);
}

void showCustomSnackBar(BuildContext context, String msg) {
  final snackBar = SnackBar(
    content: Row(
      mainAxisAlignment: MainAxisAlignment.start,
      children: [
        Icon(Icons.info_outline, size: 32),
        const SizedBox(width: 16),
        Expanded(
          child: Text(
            msg,
            style: TextStyle(fontSize: 20),
          ),
        ),
      ],
    ),
    backgroundColor: Colors.green,
    duration: Duration(seconds: 3),
    shape: StadiumBorder(),
    margin: EdgeInsets.symmetric(vertical: 16, horizontal: 12),
    behavior: SnackBarBehavior.floating,
    elevation: 0,
  );

  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(snackBar);
}

void showErrorSnackBar(BuildContext context, String msg) {
  final snackBar = SnackBar(
    content: Row(
      mainAxisAlignment: MainAxisAlignment.start,
      children: [
        Icon(Icons.error_outline, size: 32),
        const SizedBox(width: 16),
        Expanded(
          child: Text(
            msg,
            style: TextStyle(fontSize: 20),
          ),
        ),
      ],
    ),
    backgroundColor: Colors.red,
    duration: Duration(seconds: 3),
    behavior: SnackBarBehavior.fixed,
  );

  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(snackBar);
}
