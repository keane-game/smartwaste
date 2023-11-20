// ignore_for_file: prefer_const_constructors

import 'package:flutter/material.dart';
import 'package:front/my_flutter_app_icons.dart';

class BitArtHome extends StatefulWidget {
  const BitArtHome({super.key});

  @override
  State<BitArtHome> createState() => _BitArtHomeState();
}

class _BitArtHomeState extends State<BitArtHome> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Expanded(
              flex: 3,
              child: Center(
                //child: Image.asset("images/bitArt-logo.png"),

                /*child: CircleAvatar(
          backgroundImage: AssetImage("images/logo.png") ,
          radius: 80,
        ),*/
                child: Image(
                  image: AssetImage("images/logo.png"),
                  height: 150,
                ),
              )),
          Expanded(
            flex: 4,
            child: Container(
              //color: const Color(0Xffffff),
              decoration: BoxDecoration(
                  //border: Border.all( width: 1),
                  /*border: Border(
                  top: BorderSide(width: 1)

                ),*/
                  //borderRadius: BorderRadius.circular(40),
                  borderRadius: BorderRadius.only(
                      topLeft: Radius.circular(40),
                      topRight: Radius.circular(40)),
                  //color: Colors.yellow[400],
                  color: Color(0xDB2C736C)),
              //color: const Color(0x2C736C),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  Container(
                    padding: EdgeInsets.only(bottom: 40),
                    child: Text(
                      "WELCOME",
                      style: TextStyle(
                        fontSize: 25,
                        color: Colors.white,
                      ),
                    ),
                  ),
                  //Divider(height: 2,),
                  Text(
                    "\"In art, all repetition is nil\"",
                    style: TextStyle(
                      fontSize: 25,
                      color: Colors.white,
                    ),
                  ),
                  SizedBox(
                    height: 20,
                  ),
                  Text(
                    "Josué Ortago y Gasset",
                    style: TextStyle(
                      fontSize: 20,
                    ),
                  ),
                  SizedBox(
                    height: 30,
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      ElevatedButton(
                        onPressed: () {
                          Navigator.of(context).pushNamed("/register");
                        },
                        child: Text(
                          "Register",
                          style:
                              TextStyle(color: Color(0xDB2C736C), fontSize: 18),
                        ),
                        style: ButtonStyle(
                            shape: MaterialStateProperty.all<
                                RoundedRectangleBorder>(RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(20),
                              //side : BorderSide(color: Colors.black),
                            )),
                            backgroundColor:
                                MaterialStatePropertyAll<Color>(Colors.white),
                            padding: MaterialStateProperty.all(EdgeInsets.only(
                                bottom: 15, top: 10, right: 20, left: 20))

                            //textStyle: TextStyle(color: Colors.green)
                            ),
                      ),
                      SizedBox(width: 20),
                      ElevatedButton(
                        onPressed: () {},
                        child: Text(
                          "Login",
                          style: TextStyle(color: Colors.white, fontSize: 18),
                        ),
                        style: ButtonStyle(
                            shape: MaterialStateProperty.all<
                                RoundedRectangleBorder>(RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(20),
                              side: BorderSide(color: Colors.white),
                            )),
                            backgroundColor: MaterialStatePropertyAll<Color>(
                                Color(0xDB2C736C)),
                            padding: MaterialStateProperty.all(EdgeInsets.only(
                                bottom: 15, top: 10, right: 30, left: 30))

                            //textStyle: TextStyle(color: Colors.green)
                            ),
                      )
                    ],
                  ),

                  //Padding(padding: EdgeInsets.fromLTRB(l, top, right, 0))
                  Container(
                    margin: EdgeInsets.fromLTRB(50, 50, 50, 10),
                    child: Text(
                      "V1.0.0",
                      style: TextStyle(fontSize: 18, color: Colors.white),
                    ),
                  )
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}
