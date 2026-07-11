import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:sonaged/shared/widgets/bottom_navbar_items.dart';
import 'package:sonaged/shared/widgets/side_menu_tile.dart';

class AccueilScreen extends StatefulWidget {
  const AccueilScreen({super.key});

  @override
  State<AccueilScreen> createState() => _AccueilScreenState();
}

class _AccueilScreenState extends State<AccueilScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
          // title: const Icon(Icons.menu),
          ),
      drawer: Drawer(
        child: ListView(
          children: [
            UserAccountsDrawerHeader(
              accountName: const Text('Admin'),
              accountEmail: const Text('Admin'),
              currentAccountPicture: CircleAvatar(
                child: ClipOval(
                  child: Image.asset(
                    'assets/images/people.png',
                    width: 90,
                    height: 90,
                    fit: BoxFit.cover,
                  ),
                ),
              ),
              decoration: BoxDecoration(
                color: Colors.grey,
              ),
            ),
            SideMenuTile(),
          ],
        ),
      ),
      bottomNavigationBar: NavigationBar(),
    );
  }

  Widget NavigationBar() {
    return Container(
      decoration: BoxDecoration(boxShadow: [
        BoxShadow(
            color: Colors.grey.withOpacity(0.2),
            spreadRadius: 5,
            blurRadius: 11)
      ]),
      child: ClipRRect(
        child: BottomNavigationBar(
            selectedItemColor: Colors.green,
            selectedFontSize: 16,
            unselectedFontSize: 16,
            unselectedItemColor: Colors.grey.withOpacity(0.7),
            type: BottomNavigationBarType.fixed,
            items: [
              BottomNavigationBarItem(
                label: "Accuiel",
                icon: Container(
                  margin: EdgeInsets.all(5),
                  padding: EdgeInsets.all(5),
                  decoration: BoxDecoration(
                      color: Colors.grey.withOpacity(0.2),
                      borderRadius: BorderRadius.circular(10)),
                  child: Icon(
                    Icons.home,
                    size: 30,
                  ),
                ),
              ),
              BottomNavigationBarItem(
                label: "Carte",
                icon: Container(
                  margin: EdgeInsets.all(5),
                  padding: EdgeInsets.all(5),
                  decoration: BoxDecoration(
                      color: Colors.grey.withOpacity(0.2),
                      borderRadius: BorderRadius.circular(10)),
                  child: Icon(
                    Icons.map,
                    size: 30,
                  ),
                ),
              ),
              BottomNavigationBarItem(
                label: "notifs",
                icon: Container(
                  margin: EdgeInsets.all(5),
                  padding: EdgeInsets.all(5),
                  decoration: BoxDecoration(
                      color: Colors.grey.withOpacity(0.2),
                      borderRadius: BorderRadius.circular(10)),
                  child: Icon(
                    Icons.notifications,
                    size: 30,
                  ),
                ),
              ),
              BottomNavigationBarItem(
                label: "Profil",
                icon: Container(
                  margin: EdgeInsets.all(5),
                  padding: EdgeInsets.all(5),
                  decoration: BoxDecoration(
                      color: Colors.grey.withOpacity(0.2),
                      borderRadius: BorderRadius.circular(10)),
                  child: Icon(
                    Icons.person,
                    size: 30,
                  ),
                ),
              ),
            ]),
      ),
    );
  }
}
