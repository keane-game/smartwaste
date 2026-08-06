import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/app_colors.dart';
import 'package:sonaged/configs/constants/style_constant.dart';
import 'package:sonaged/configs/constants/text_constant.dart';
import 'package:sonaged/features/account/presentation/widgets/or_divider.dart';
import 'package:sonaged/features/auth/presentation/providers/login_provider.dart';
import 'package:sonaged/features/auth/presentation/screens/login_screen.dart';
import 'package:sonaged/shared/widgets/already_have_an_account_acheck.dart';

class SignUpForm extends StatelessWidget {
  const SignUpForm({
    super.key,
    required this.emailController,
    required this.passwordController,
    required this.fullnameController,
    required this.addresseController,
    required this.state,
    required this.ref,
  });

  final TextEditingController emailController;
  final TextEditingController passwordController;
  final TextEditingController fullnameController;
  final TextEditingController addresseController;

  final dynamic state;
  final dynamic ref;

  @override
  Widget build(BuildContext context) {
    return Form(
      child: Column(
        children: [
          TextFormField(
            textInputAction: TextInputAction.next,
            cursorColor: AppColors.kPrimaryColor,
            decoration: const InputDecoration(
              hintText: tFullName,
              prefixIcon: Padding(
                padding: EdgeInsets.all(defaultPadding),
                child: Icon(Icons.person),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: defaultPadding),
            child: TextFormField(
              keyboardType: TextInputType.emailAddress,
              textInputAction: TextInputAction.next,
              onSaved: (tUsername) {},
              cursorColor: AppColors.kPrimaryColor,
              decoration: const InputDecoration(
                hintText: tUsername,
                prefixIcon: Padding(
                  padding: EdgeInsets.all(defaultPadding),
                  child: Icon(Icons.email),
                ),
              ),
            ),
          ),
          TextFormField(
            textInputAction: TextInputAction.next,
            cursorColor: AppColors.kPrimaryColor,
            obscureText: true,
            decoration: const InputDecoration(
              hintText: tPassword,
              prefixIcon: Padding(
                padding: EdgeInsets.all(defaultPadding),
                child: Icon(Icons.lock),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: defaultPadding),
            child: TextFormField(
              textInputAction: TextInputAction.done,
              cursorColor: AppColors.kPrimaryColor,
              decoration: const InputDecoration(
                hintText: tAdress,
                prefixIcon: Padding(
                  padding: EdgeInsets.all(defaultPadding),
                  child: Icon(Icons.home),
                ),
              ),
            ),
          ),
          const SizedBox(height: defaultPadding / 2),
          const SizedBox(height: defaultPadding + 5),
          state.maybeMap(
            loading: (_) => const Center(child: CircularProgressIndicator()),
            orElse: () => loginButton(ref),
          ),
          const SizedBox(height: defaultPadding - 5),
          const OrDivider(),
          const SizedBox(height: defaultPadding - 5),
          AlreadyHaveAnAccountCheck(
            login: false,
            press: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) {
                    return LoginScreen();
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
      child: Text(
        tSignupBtn.toUpperCase(),
        style: const TextStyle(fontSize: tDefaultSize * 0.5),
      ),
    );
  }
}
