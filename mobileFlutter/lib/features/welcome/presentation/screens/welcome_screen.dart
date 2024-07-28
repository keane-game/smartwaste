import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:sonaged/configs/constants/image_contant.dart';
import 'package:sonaged/configs/constants/text_constant.dart';

class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
          body: Stack(
        children: <Widget>[
          Container(
            decoration: const BoxDecoration(
              image: DecorationImage(
                  image: AssetImage(tWelcomeScreenImage),
                  fit: BoxFit.cover,
                  scale: 5.0),
            ),
          ),
          Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                const Padding(
                  padding: EdgeInsets.only(bottom: 30),
                  child: Text(
                    tWelcome,
                    style: TextStyle(
                      fontSize: 35,
                      color: Colors.white,
                      fontStyle: FontStyle.italic,
                      fontWeight: FontWeight.w600,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ),
                const Padding(
                  padding: EdgeInsets.only(bottom: 50),
                  child: Text(
                    tSlogan,
                    style: TextStyle(
                      fontSize: 20,
                      color: Colors.white,
                      fontStyle: FontStyle.italic,
                      fontWeight: FontWeight.w400,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ),
                const SizedBox(
                  height: 30,
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    _signInButton(context),
                  ],
                ),
                const SizedBox(
                  height: 40,
                ),
              ],
            ),
          )
        ],
      )),
    );
  }

  Widget _signInButton(BuildContext context) {
    return OutlinedButton(
      onPressed: () {
        context.goNamed('login');
        //print("object");
      },
      style: OutlinedButton.styleFrom(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
        backgroundColor: Colors.white,
        fixedSize: const Size(300, 65),
      ),
      child: const Padding(
        padding: EdgeInsets.fromLTRB(0, 0, 0, 0),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Padding(
              padding: EdgeInsets.fromLTRB(20, 10, 20, 10),
              child: Text(
                'Démarrer',
                style: TextStyle(
                    fontSize: 30,
                    color: Color(0xDB5D8B47),
                    fontWeight: FontWeight.w500),
              ),
            )
          ],
        ),
      ),
    );
  }
}
