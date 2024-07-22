import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:sonaged/configs/constants/text_constant.dart';
import 'package:sonaged/features/auth/presentation/providers/login_provider.dart';
import 'package:sonaged/features/auth/presentation/providers/state/auth_state.dart';
import 'package:sonaged/features/auth/presentation/widgets/login_widget.dart';
import 'package:sonaged/shared/widgets/responsive.dart';
import 'package:sonaged/shared/widgets/background.dart';

class LoginScreen extends ConsumerStatefulWidget {
  LoginScreen({super.key});

  @override
  _LoginScreenState createState() => _LoginScreenState();
}

final emailController = TextEditingController();
final passwordController = TextEditingController();

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(authStateNotifierProvider);
    ref.listen(
      authStateNotifierProvider.select((value) => value),
      ((previous, next) {
        //show Snackbar on failure
        if (next is Failure) {
          ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(next.exception.message.toString())));
        } else if (next is Success) {
          context.go("/dashboard");
        }
      }),
    );
    return Background(
      child: SingleChildScrollView(
        child: Responsive(
          mobile: MobileLoginScreen(
            child: LoginForm(
              formKey: _formKey,
              emailController: emailController,
              passwordController: passwordController,
              state: state,
              ref: ref,
            ),
          ),
          desktop: Row(
            children: [
              const Expanded(
                child: LoginScreenTopImage(),
              ),
              Expanded(
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    SizedBox(
                      width: 450,
                      child: LoginForm(
                        formKey: _formKey,
                        emailController: emailController,
                        passwordController: passwordController,
                        state: state,
                        ref: ref,
                      ),
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

Widget loginButton(WidgetRef ref) {
  return ElevatedButton(
    onPressed: () {
      ref.read(authStateNotifierProvider.notifier).loginUser(
            emailController.text,
            passwordController.text,
          );
    },
    style: ElevatedButton.styleFrom(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
      backgroundColor: const Color(0xDB5D8B47),
      fixedSize: const Size(300, 65),
    ),
    child: const Text(tLoginBtn),
  );
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
