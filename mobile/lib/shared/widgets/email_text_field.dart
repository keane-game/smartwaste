import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/app_colors.dart';
import 'package:sonaged/configs/constants/style_constant.dart';
import 'package:sonaged/configs/constants/text_constant.dart';

class EmailTextField extends StatefulWidget {
  const EmailTextField({super.key, required this.emailController});

  final TextEditingController emailController;

  @override
  EmailTextFieldState createState() => EmailTextFieldState();
}

class EmailTextFieldState extends State<EmailTextField> {
  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: widget.emailController,
      keyboardType: TextInputType.emailAddress,
      textInputAction: TextInputAction.next,
      cursorColor: AppColors.kPrimaryColor,
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Veuillez entrer votre email';
        } else if (!RegExp(r'^[^@]+@[^@]+\.[^@]+').hasMatch(value)) {
          return 'Veuillez entrer un email valide';
        }
        return null;
      },
      decoration: const InputDecoration(
        border: OutlineInputBorder(
          borderSide: BorderSide(width: 1, color: AppColors.kPrimaryLightColor),
          borderRadius: BorderRadius.all(Radius.circular(tDefaultSize)),
        ),
        hintText: tEmail,
        prefixIcon: Padding(
          padding: EdgeInsets.all(defaultPadding),
          child: Icon(
            Icons.person,
            color: Colors.grey,
          ),
        ),
      ),
    );
  }
}
