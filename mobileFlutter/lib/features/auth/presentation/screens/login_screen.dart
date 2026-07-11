import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:sonaged/features/auth/presentation/providers/login_provider.dart';
import 'package:sonaged/features/auth/presentation/providers/state/auth_state.dart';
import 'package:sonaged/features/auth/presentation/widgets/login_widget.dart';
import 'package:sonaged/shared/widgets/custom_snackbar.dart';
import 'package:sonaged/shared/widgets/responsive.dart';
import 'package:sonaged/shared/widgets/background.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  LoginScreenState createState() => LoginScreenState();
}

final emailController = TextEditingController();
final passwordController = TextEditingController();

class LoginScreenState extends ConsumerState<LoginScreen> {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    ref.listen(
      authStateNotifierProvider.select((value) => value),
      ((previous, next) {
        //show Snackbar on failure
        if (next is Failure) {
          // ScaffoldMessenger.of(context).showSnackBar(
          //     SnackBar(content: Text(next.exception.message.toString())));
          showErrorSnackBar(context, next.exception.message.toString());
        } else if (next is Success) {
          context.go("/dashboard");
        }
      }),
    );
    return const Background(
      child: SingleChildScrollView(
        child: Responsive(
          mobile: MobileLoginScreen(
            child: LoginForm(),
          ),
          desktop: Row(
            children: [
              Expanded(
                child: LoginScreenTopImage(),
              ),
              Expanded(
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    SizedBox(
                      width: 450,
                      child: LoginForm(),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class MobileLoginScreen extends StatelessWidget {
  const MobileLoginScreen({
    super.key,
    required this.child,
  });

  final Widget child;
  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: <Widget>[
        const LoginScreenTopImage(),
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
      ],
    );
  }
}
