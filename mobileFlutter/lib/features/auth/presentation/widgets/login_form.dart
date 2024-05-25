import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/app_colors.dart';
import 'package:sonaged/configs/constants/style_constant.dart';
import 'package:sonaged/configs/constants/text_constant.dart';
import 'package:sonaged/features/account/presentation/signup_screen.dart';
import 'package:sonaged/features/auth/presentation/widgets/or_divider.dart';
import 'package:sonaged/features/auth/presentation/providers/login_provider.dart';
import 'package:sonaged/shared/widgets/already_have_an_account_acheck.dart';

class LoginForm extends StatelessWidget {
  const LoginForm({
    super.key,
    required this.emailController,
    required this.passwordController,
    required this.state,
    required this.ref,
  });

  final TextEditingController emailController;
  final TextEditingController passwordController;

  final dynamic state;
  final dynamic ref;

  @override
  Widget build(BuildContext context) {
    return Form(
      child: Column(
        children: [
          TextFormField(
            controller: emailController,
            keyboardType: TextInputType.emailAddress,
            textInputAction: TextInputAction.next,
            cursorColor: AppColors.kPrimaryColor,
            onSaved: (email) {},
            decoration: const InputDecoration(
              hintText: tEmail,
              prefixIcon: Padding(
                padding: EdgeInsets.all(defaultPadding),
                child: Icon(
                  Icons.person,
                  color: Colors.grey,
                ),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: defaultPadding),
            child: TextFormField(
              controller: passwordController,
              textInputAction: TextInputAction.done,
              obscureText: true,
              cursorColor: AppColors.kPrimaryColor,
              decoration: const InputDecoration(
                hintText: tPassword,
                prefixIcon: Padding(
                  padding: EdgeInsets.all(defaultPadding),
                  child: Icon(
                    Icons.lock,
                    color: Colors.grey,
                  ),
                ),
              ),
            ),
          ),
          SizedBox(
            width: MediaQuery.of(context).size.width * 0.8,
            child: GestureDetector(
              onTap: () {},
              child: const Text(
                tForgetPassword,
                textAlign: TextAlign.end,
                style: TextStyle(
                  color: AppTextStyles.kTextFieldFill,
                  fontWeight: FontWeight.w500,
                  decoration: TextDecoration.underline,
                ),
              ),
            ),
          ),
          const SizedBox(height: defaultPadding + 5),
          state.maybeMap(
            loading: (_) => const Center(child: CircularProgressIndicator()),
            orElse: () => loginButton(ref),
          ),
          const SizedBox(height: defaultPadding - 5),
          const OrDivider(),
          const SizedBox(height: defaultPadding - 5),
          AlreadyHaveAnAccountCheck(
            press: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) {
                    return SignUpScreen();
                  },
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  Widget loginButton(dynamic ref) {
    return ElevatedButton(
      onPressed: () {
        // print("Email: " + emailController.text);
        // print(passwordController.text);
        // validate email and password
        ref.read(authStateNotifierProvider.notifier).loginUser(
              emailController.text,
              passwordController.text,
            );
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
}
