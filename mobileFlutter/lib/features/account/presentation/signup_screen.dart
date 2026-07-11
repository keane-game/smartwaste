import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sonaged/configs/constants/style_constant.dart';
import 'package:sonaged/features/auth/presentation/providers/login_provider.dart';
import 'package:sonaged/shared/widgets/responsive.dart';
import 'package:sonaged/shared/widgets/background.dart';
import 'widgets/sign_up_top_image.dart';
import 'widgets/signup_form.dart';

class SignUpScreen extends ConsumerWidget {
  SignUpScreen({super.key});

  final emailController = TextEditingController();
  final passwordController = TextEditingController();
  final fullnameController = TextEditingController();
  final addresseController = TextEditingController();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(authStateNotifierProvider);
    return Background(
      child: SingleChildScrollView(
        child: Responsive(
          mobile: MobileSignupScreen(
            child: SignUpForm(
                emailController: emailController,
                passwordController: passwordController,
                addresseController: addresseController,
                fullnameController: fullnameController,
                state: state,
                ref: ref),
          ),
          desktop: Row(
            children: [
              const Expanded(
                child: SignUpScreenTopImage(),
              ),
              Expanded(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    SizedBox(
                      width: 450,
                      child: SignUpForm(
                        emailController: emailController,
                        passwordController: passwordController,
                        addresseController: addresseController,
                        fullnameController: fullnameController,
                        state: state,
                        ref: ref,
                      ),
                    ),
                    const SizedBox(height: defaultPadding / 2),
                    // SocalSignUp()
                  ],
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}

class MobileSignupScreen extends StatelessWidget {
  const MobileSignupScreen({
    super.key,
    required this.child,
  });

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: <Widget>[
        const SignUpScreenTopImage(),
        Row(
          children: [
            const Spacer(),
            Expanded(
              flex: 8,
              child: child,
            ),
            const Spacer(),
          ],
        ),
        // const SocalSignUp()
      ],
    );
  }
}
