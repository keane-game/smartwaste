import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/app_colors.dart';

BoxDecoration appBoxShadow(
    {Color color = AppColors.kPrimaryColor,
    double radius = 15,
    double sR = 1,
    double bR = 2,
    BoxBorder? border}) {
  return BoxDecoration(color: color, border: border, boxShadow: [
    BoxShadow(
        color: Colors.grey.withOpacity(0.1),
        spreadRadius: sR,
        blurRadius: bR,
        offset: const Offset(0, 1))
  ]);
}

BoxDecoration appBoxShadowWithRaduis(
    {Color color = AppColors.kPrimaryColor,
    double radius = 15,
    double sR = 1,
    double bR = 2,
    BoxBorder? border}) {
  return BoxDecoration(
      color: color,
      borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(20), topRight: Radius.circular(20)),
      border: border,
      boxShadow: [
        BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: sR,
            blurRadius: bR,
            offset: const Offset(0, 1))
      ]);
}
