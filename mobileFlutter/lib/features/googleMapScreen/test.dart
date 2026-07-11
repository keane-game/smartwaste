import 'package:flutter/material.dart';
import 'package:sonaged/features/googleMapScreen/google-map-screen.dart';
import 'package:sonaged/features/chooseUserScreen/choose-user-screen.dart';

class TestScreen extends StatelessWidget {
  const TestScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Maps',
      theme: ThemeData(
        brightness: Brightness.dark,
        primaryColor: Colors.green[800],
      ),
      home: const MyHomePage(title: 'Live Tracking'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({required this.title, super.key});
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      //appBar: AppBar(
      //title: Text(widget.title),
      //backgroundColor: Colors.black,
      //),
      body: Container(
        padding: const EdgeInsets.fromLTRB(20, 20, 20, 20),
        //use Container for back-image
        decoration: const BoxDecoration(
          image: DecorationImage(
            image: AssetImage("assets/images/pexels1.jpg"),
            fit: BoxFit.fill,
          ),
        ),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.end,
            children: <Widget>[
              SizedBox(
                width: double.infinity,
                height: 55,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(15)),
                    backgroundColor: const Color(0xDB5D8B47),
                    fixedSize: const Size(300, 65),
                  ),
                  child: const Text(
                    "GOOGLE MAP SCREEN",
                    style: TextStyle(fontSize: 20),
                  ),
                  onPressed: () {
                    Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (context) => const GoogleMapScreen()));
                  },
                ),
              ),
              const Padding(
                padding: EdgeInsets.all(8.0),
              ),
              SizedBox(
                  width: double.infinity,
                  height: 55,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(15)),
                      backgroundColor: const Color(0xDB5D8B47),
                      fixedSize: const Size(300, 65),
                    ),
                    child: const Text(
                      "LIVE TRACKING",
                      style: TextStyle(fontSize: 20),
                    ),
                    onPressed: () {
                      Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (context) => ChooseUser()));
                    },
                  ))
            ],
          ),
        ),
      ),
    );
  }
}
