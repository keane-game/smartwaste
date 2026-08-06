import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/style_constant.dart';
import 'package:sonaged/configs/constants/text_constant.dart';

class AlreadyHaveAnAccountCheck extends StatelessWidget {
  final bool login;
  final Function? press;
  const AlreadyHaveAnAccountCheck({
    super.key,
    this.login = true,
    required this.press,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: <Widget>[
        Text(
          login ? tDontHaveAnAccount : tAlreadyHaveAnAccount,
          style: const TextStyle(color: AppTextStyles.kTextFieldFill),
        ),
        GestureDetector(
          onTap: press as void Function()?,
          child: Text(
            login ? tSignup : tLogin,
            style: const TextStyle(
              color: AppTextStyles.kTextFieldFill,
              fontWeight: FontWeight.bold,
            ),
          ),
        )
      ],
    );
  }
}
