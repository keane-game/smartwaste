import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/configs/constants/app_colors.dart';
import 'package:sonaged/configs/constants/style_constant.dart';
import 'package:sonaged/configs/constants/text_constant.dart';
import 'package:sonaged/features/account/presentation/signup_screen.dart';
import 'package:sonaged/features/auth/presentation/providers/state/auth_state.dart';
import 'package:sonaged/features/auth/presentation/widgets/or_divider.dart';
import 'package:sonaged/features/auth/presentation/providers/login_provider.dart';
import 'package:sonaged/shared/widgets/already_have_an_account_acheck.dart';

class LoginForm extends StatelessWidget {
  const LoginForm({
    super.key,
    required this.formKey,
    required this.emailController,
    required this.passwordController,
    required this.state,
    required this.ref,
  });

  final GlobalKey<FormState> formKey;
  final TextEditingController emailController;
  final TextEditingController passwordController;
  final AuthState state;
  final WidgetRef ref;

  @override
  Widget build(BuildContext context) {
    return Form(
      key: formKey,
      child: Column(
        children: [
          TextFormField(
            controller: emailController,
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
              hintText: tEmail,
              prefixIcon: Padding(
                padding: EdgeInsets.all(defaultPadding),
                child: Icon(
                  Icons.person,
                  color: Colors.grey,
                ),
              ),
              errorStyle: TextStyle(
                  color: Colors.red), // Style personnalisé pour les erreurs
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
                errorStyle: TextStyle(
                    color: Colors.red), // Style personnalisé pour les erreurs
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Veuillez entrer votre mot de passe';
                } else if (value.length < 6) {
                  return 'Le mot de passe doit contenir au moins 6 caractères';
                }
                return null;
              },
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
            orElse: () => loginButton(ref, formKey),
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

  Widget loginButton(dynamic ref, formKey) {
    return ElevatedButton(
      onPressed: () {
        if (formKey.currentState!.validate()) {
          ref.read(authStateNotifierProvider.notifier).loginUser(
                emailController.text,
                passwordController.text,
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
}
