import 'package:flutter/material.dart';

class MyText extends StatelessWidget {
  const MyText(
      {super.key,
      required this.text,
      required this.textStyle,
      required this.padding});
  final String text;
  final TextStyle textStyle;
  final double padding;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(bottom: padding),
      child: Text(text, style: textStyle),
    );
  }
}
