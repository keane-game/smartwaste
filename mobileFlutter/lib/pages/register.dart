import 'package:flutter/material.dart';
import 'package:front/my_flutter_app_icons.dart';

class Register extends StatefulWidget {
  const Register({super.key});

  @override
  State<Register> createState() => _RegisterState();
}

class _RegisterState extends State<Register> {
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
                                  labelText: 'First name',
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                            flex: 1,
                            child: Container(
                              //width: 10,
                              padding: const EdgeInsets.only(
                                  top: 15, right: 2, bottom: 15),
                              margin: const EdgeInsets.only(right: 17),
                              child: const Icon(
                                Icons.account_circle,
                                size: 30,
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
                                  labelText: 'Last name',
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                            flex: 1,
                            child: Container(
                              //width: 10,
                              padding: const EdgeInsets.only(
                                  top: 15, right: 2, bottom: 15),
                              margin: const EdgeInsets.only(right: 17),
                              child: const Icon(
                                Icons.account_circle,
                                size: 30,
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
                                  labelText: 'Email',
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                            flex: 1,
                            child: Container(
                              //width: 10,
                              padding: const EdgeInsets.only(
                                  top: 15, right: 2, bottom: 15),
                              margin: const EdgeInsets.only(right: 17),
                              child: const Icon(
                                Icons.email,
                                size: 30,
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
                                  labelText: 'Phone',
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                            flex: 1,
                            child: Container(
                              //width: 10,
                              padding: const EdgeInsets.only(
                                  top: 15, right: 2, bottom: 15),
                              margin: const EdgeInsets.only(right: 17),
                              child: const Icon(
                                Icons.phone,
                                size: 30,
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
                                  labelText: 'Password',
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                            flex: 1,
                            child: Container(
                              //width: 10,
                              padding: const EdgeInsets.only(
                                  top: 15, right: 2, bottom: 15),
                              margin: const EdgeInsets.only(right: 17),
                              child: const Icon(
                                Icons.lock,
                                size: 30,
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
