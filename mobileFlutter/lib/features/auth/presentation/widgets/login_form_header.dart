import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/image_contant.dart';
import 'package:sonaged/configs/constants/text_constant.dart';

class LoginFormHeader extends StatelessWidget {
  const LoginFormHeader({
    super.key,
    required this.size,
  });

  final Size size;

  @override
  Widget build(BuildContext context) {
    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Image(
        image: const AssetImage(tSplashImage),
        height: size.height * 0.2,
      ),
      const Text(tLogin),
      const Text(tLoginSubTitle),
    ]);
  }
}
