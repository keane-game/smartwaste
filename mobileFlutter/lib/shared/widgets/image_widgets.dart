import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/app_colors.dart';

Widget appImage({
  String imagePath = "assets/images/people.png",
  double width = 16,
  double height = 16,
}) {
  return Image.asset(
    imagePath.isNotEmpty ? "assets/images/people.png" : imagePath,
    width: width,
    height: height,
  );
}

Widget appImageWithColor(
    {String imagePath = "assets/images/people.png",
    double width = 26,
    double height = 26,
    Color color = AppColors.kPrimaryColor}) {
  return Image.asset(
    imagePath.isNotEmpty ? "assets/images/people.png" : imagePath,
    width: width,
    height: height,
    color: color,
  );
}
