import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/configs/constants/style_constant.dart';
import 'package:sonaged/configs/constants/text_constant.dart';
import 'package:sonaged/features/account/presentation/signup_screen.dart';
import 'package:sonaged/features/auth/presentation/providers/state/auth_state.dart';
import 'package:sonaged/features/auth/presentation/widgets/login_form_button.dart';
import 'package:sonaged/features/auth/presentation/widgets/or_divider.dart';
import 'package:sonaged/features/auth/presentation/providers/login_provider.dart';
import 'package:sonaged/shared/widgets/already_have_an_account_acheck.dart';
import 'package:sonaged/shared/widgets/email_text_field.dart';
import 'package:sonaged/shared/widgets/password_text_field.dart';

class LoginForm extends ConsumerStatefulWidget {
  const LoginForm({super.key});

  @override
  LoginFormState createState() => LoginFormState();
}

class LoginFormState extends ConsumerState<LoginForm> {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(authStateNotifierProvider);
    return Form(
      key: _formKey,
      child: Column(
        children: [
          EmailTextField(emailController: emailController),
          const SizedBox(height: defaultPadding + 5),
          PasswordTextField(passwordController: passwordController),
          const SizedBox(height: defaultPadding),
          SizedBox(
            width: MediaQuery.of(context).size.width * 0.75,
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
            orElse: () => LoginFormButton(
                ref, _formKey, emailController.text, passwordController.text),
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
}
