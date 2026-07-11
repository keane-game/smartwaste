import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/app_colors.dart';
import 'package:sonaged/configs/constants/style_constant.dart';
import 'package:sonaged/features/auth/presentation/providers/login_provider.dart';

Widget LoginFormButton(dynamic ref, formKey, String email, String password) {
  return ElevatedButton(
    onPressed: () {
      if (formKey.currentState!.validate()) {
        ref.read(authStateNotifierProvider.notifier).loginUser(
              email,
              password,
            );
      }
    },
    style: ElevatedButton.styleFrom(
      shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(tDefaultSize)),
      backgroundColor: AppColors.kPrimaryColor,
      fixedSize: const Size(300, 65),
    ),
    child: const Text(
      'SE CONNECTER',
      style: TextStyle(fontSize: tDefaultSize * 0.5),
    ),
  );
}
