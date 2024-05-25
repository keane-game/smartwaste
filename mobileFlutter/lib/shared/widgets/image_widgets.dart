import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/app_colors.dart';

Widget appImage({
  String imagePath = "assets/icons/user.png",
  double width = 16,
  double height = 16,
}) {
  return Image.asset(
    imagePath.isNotEmpty ? "assets/icons/user.png" : imagePath,
    width: width,
    height: height,
  );
}

Widget appImageWithColor(
    {String imagePath = "assets/icons/user.png",
    double width = 16,
    double height = 16,
    Color color = AppColors.kPrimaryColor}) {
  return Image.asset(
    imagePath.isNotEmpty ? "assets/icons/user.png" : imagePath,
    width: width,
    height: height,
    color: color,
  );
}
