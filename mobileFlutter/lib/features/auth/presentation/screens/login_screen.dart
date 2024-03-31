// ignore_for_file: prefer_const_constructors, use_key_in_widget_constructors, sort_child_properties_last, prefer_const_literals_to_create_immutables

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:sonaged/features/auth/presentation/providers/login_provider.dart';
import 'package:sonaged/features/auth/presentation/providers/state/auth_state.dart';
import 'package:sonaged/features/auth/presentation/widgets/login_field.dart';

class LoginScreen extends ConsumerWidget {
  LoginScreen({super.key});

  final TextEditingController emailController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final size = MediaQuery.of(context).size;
    final state = ref.watch(authStateNotifierProvider);
    ref.listen(
      authStateNotifierProvider.select((value) => value),
      ((previous, next) {
        //show Snackbar on failure
        if (next is Failure) {
          ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(next.exception.message.toString())));
        } else if (next is Success) {
          (context).goNamed("dashoard");
        }
      }),
    );
    return Scaffold(
        body: SafeArea(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          Expanded(
            flex: 2,
            child: Center(
              child: Image(
                image: AssetImage("assets/images/sonadeg-img.png"),
              ),
            ),
          ),
          AuthField(
            hintText: 'Username',
            controller: emailController,
          ),
          AuthField(
            hintText: 'Password',
            obscureText: true,
            controller: passwordController,
          ),
          state.maybeMap(
            loading: (_) => const Center(child: CircularProgressIndicator()),
            orElse: () => loginButton(ref),
          ),
        ],
      ),
    ));
  }

  Widget loginButton(WidgetRef ref) {
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
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
        backgroundColor: Color(0xDB5D8B47),
        fixedSize: const Size(300, 65),
      ),
      child: const Text('SE CONNECTER'),
    );
  }
}
