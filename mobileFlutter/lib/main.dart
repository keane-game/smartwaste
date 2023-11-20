<<<<<<< HEAD

// ignore_for_file: unused_import

import 'package:flutter/material.dart';
import 'package:front/pages/login_page.dart';
import 'package:front/pages/my_flutter_app_icons.dart';

//import 'package:hexcolor/hexcolor.dart';


void main() {

  runApp(MaterialApp(
<<<<<<< HEAD
    routes: <String, WidgetBuilder>{
      "/LoginPage": (BuildContext context) => const BitArtLoginPage(),
     //  "/home": (BuildContext context) => const BitArtHome(),
      //"/register": (BuildContext context) => const Register(),
      // "/secret": (BuildContext context) => const SecretPage(),
    },
    initialRoute: "/LoginPage",
=======
// ignore_for_file: sort_child_properties_last, use_key_in_widget_constructors

import 'package:flutter/material.dart';
import 'package:front/my_flutter_app_icons.dart';
import 'package:front/pages/home.dart';
import 'package:front/pages/register.dart';

//import 'package:hexcolor/hexcolor.dart';

// code du home page  de bit'Art
void main() {
  // ignore: prefer_const_constructors
  // runApp(MaterialApp(
  //   //home: BitArtHome(),
  //   debugShowCheckedModeBanner: false,
  //   home: const BitArtHome(),
  // ));

  runApp(MaterialApp(
    routes: <String, WidgetBuilder>{
      "/home": (BuildContext context) => const BitArtHome(),
      "/register": (BuildContext context) => const Register(),
      // "/secret": (BuildContext context) => const SecretPage(),
    },
    initialRoute: "/home",
>>>>>>> b1c22c0 ( work again in progress)
    title: 'Bit\'Art',
    debugShowCheckedModeBanner: false,
    // theme: ThemeData(
    //   primarySwatch: Colors.blue,
    // ),
<<<<<<< HEAD
    home: const BitArtLoginPage(),
  ));
}

=======
    home: BitArtHome(),
      debugShowCheckedModeBanner: false
    //home: BitArtCreateAccount(),
=======
    home: const BitArtHome(),
>>>>>>> b1c22c0 ( work again in progress)
  ));
}

/*
class BitArtCreateAccount extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xDB2C736C),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Expanded(
            flex: 2,
            child: Container(
              child: const Center(
                child: Image(
                  image: AssetImage("images/logo-small.png"),
                ),
              ),
            ),
          ),
          Expanded(
            flex: 6,
            child: Container(
                decoration: const BoxDecoration(
                    borderRadius: BorderRadius.only(
                      topRight: Radius.circular(40),
                      topLeft: Radius.circular(40),
                    ),
                    color: Colors.white),
                child: Column(
                  children: [
                    Container(
                        padding: const EdgeInsets.only(top: 30),
                        child: const Text(
                          "CREATE ACCOUNT",
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                          ),
                        )),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Expanded(
                          flex: 4,
                          child: Container(
                            //padding : EdgeInsets.fromLTRB(40, 30, 0,0) ,
                            padding: const EdgeInsets.only(top: 30, left: 30),
                            child: const SizedBox(
                              width: 300,
                              child: TextField(
                                obscureText: true,
                                decoration: InputDecoration(
                                  border: OutlineInputBorder(
                                      borderRadius: BorderRadius.only(
                                          topLeft: Radius.circular(5),
                                          bottomLeft: Radius.circular(5))),
                                  labelText: 'First namee',
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                            flex: 1,
                            child: Container(
                              //width: 10,
                              padding: const EdgeInsets.only(top: 8, right: 2),
                              margin: const EdgeInsets.only(right: 17),
                              child: const Icon(
                                Icons.account_circle,
                                size: 51,
                                color: Colors.white,
                              ),
                              decoration: const BoxDecoration(
                                  //border : Border.all(width : 1, color : Colors.black),
                                  color: Color(0xDB2C736C),
                                  borderRadius: BorderRadius.only(
                                    topRight: Radius.circular(10),
                                  )),
                            ))
                      ],
                    ),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Expanded(
                          flex: 4,
                          child: Container(
                            //padding : EdgeInsets.fromLTRB(40, 30, 0,0) ,
                            padding: const EdgeInsets.only(top: 30, left: 30),
                            child: const SizedBox(
                              width: 300,
                              child: TextField(
                                obscureText: true,
                                decoration: InputDecoration(
                                  border: OutlineInputBorder(
                                      borderRadius: BorderRadius.only(
                                          topLeft: Radius.circular(5),
                                          bottomLeft: Radius.circular(5))),
                                  labelText: 'First namee',
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                            flex: 1,
                            child: Container(
                              //width: 10,
                              padding: const EdgeInsets.only(top: 8, right: 2),
                              margin: const EdgeInsets.only(right: 17),
                              child: const Icon(
                                Icons.account_circle,
                                size: 51,
                                color: Colors.white,
                              ),
                              decoration: const BoxDecoration(
                                  //border : Border.all(width : 1, color : Colors.black),
                                  color: Color(0xDB2C736C),
                                  borderRadius: BorderRadius.only(
                                    topRight: Radius.circular(10),
                                  )),
                            ))
                      ],
                    ),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Expanded(
                          flex: 4,
                          child: Container(
                            //padding : EdgeInsets.fromLTRB(40, 30, 0,0) ,
                            padding: const EdgeInsets.only(top: 30, left: 30),
                            child: const SizedBox(
                              width: 300,
                              child: TextField(
                                obscureText: true,
                                decoration: InputDecoration(
                                  border: OutlineInputBorder(
                                      borderRadius: BorderRadius.only(
                                          topLeft: Radius.circular(5),
                                          bottomLeft: Radius.circular(5))),
                                  labelText: 'First namee',
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                            flex: 1,
                            child: Container(
                              //width: 10,
                              padding: const EdgeInsets.only(top: 8, right: 2),
                              margin: const EdgeInsets.only(right: 17),
                              child: const Icon(
                                Icons.account_circle,
                                size: 51,
                                color: Colors.white,
                              ),
                              decoration: const BoxDecoration(
                                  //border : Border.all(width : 1, color : Colors.black),
                                  color: Color(0xDB2C736C),
                                  borderRadius: BorderRadius.only(
                                    topRight: Radius.circular(10),
                                  )),
                            ))
                      ],
                    ),
                  ],
                )),
          )
        ],
      ),
    );
  }
}
<<<<<<< HEAD
>>>>>>> 702e740 (new works frontend)
=======
*/
>>>>>>> b1c22c0 ( work again in progress)
