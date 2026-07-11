import 'package:flutter/material.dart';

AppBar builAppbar() {
  return AppBar(
    bottom: PreferredSize(
        preferredSize: const Size.fromHeight(1),
        child: Container(
          color: Colors.grey.withOpacity(0.3),
          height: 1,
        )),
  );
}
