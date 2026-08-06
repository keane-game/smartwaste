import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/app_colors.dart';
import 'package:sonaged/configs/constants/style_constant.dart';
import 'package:sonaged/configs/constants/text_constant.dart';

class PasswordTextField extends StatefulWidget {
  const PasswordTextField({super.key, required this.passwordController});

  final TextEditingController passwordController;

  @override
  PasswordTextFieldState createState() => PasswordTextFieldState();
}

class PasswordTextFieldState extends State<PasswordTextField> {
  @override
  Widget build(BuildContext context) {
    bool passToggle = true;
    return TextFormField(
      controller: widget.passwordController,
      textInputAction: TextInputAction.done,
      obscureText: passToggle,
      cursorColor: AppColors.kPrimaryColor,
      decoration: InputDecoration(
        hintText: tPassword,
        border: const OutlineInputBorder(
          borderSide: BorderSide(width: 1, color: AppColors.kPrimaryLightColor),
          borderRadius: BorderRadius.all(Radius.circular(tDefaultSize)),
        ),
        prefixIcon: const Padding(
          padding: EdgeInsets.all(defaultPadding),
          child: Icon(
            Icons.lock,
            color: Colors.grey,
          ),
        ),
        suffixIcon: GestureDetector(
          onTap: () {
            setState(() {
              passToggle = !passToggle;
            });
          },
          child: Padding(
            padding: const EdgeInsets.all(0),
            child: Icon(
                size: 18,
                // ignore: dead_code
                passToggle ? Icons.visibility : Icons.visibility_off),
          ),
        ),
      ),
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Veuillez entrer votre mot de passe';
        } else if (value.length < 5) {
          return 'Le mot de passe doit contenir au moins 5 caractères';
        }
        return null;
      },
    );
  }
}
