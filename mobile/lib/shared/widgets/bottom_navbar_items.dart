import 'package:flutter/material.dart';
import 'package:sonaged/shared/widgets/image_widgets.dart';

var bottomNavbarItems = <BottomNavigationBarItem>[
  BottomNavigationBarItem(
      label: 'Accueil',
      icon: SizedBox(width: 35, height: 35, child: appImageWithColor())),
  BottomNavigationBarItem(
      label: 'Calendrier',
      icon: SizedBox(width: 35, height: 35, child: appImageWithColor())),
  BottomNavigationBarItem(
      label: 'Map',
      icon: SizedBox(
          width: 35,
          height: 35,
          child: Icon(
            Icons.calendar_month,
            color: Colors.grey,
          ))),
  BottomNavigationBarItem(
      label: 'Profil',
      icon: SizedBox(width: 35, height: 35, child: appImageWithColor())),
];

// bottom_navbar_items.dart
class BottomNavBarItems {
  static List<BottomNavigationBarItem> get items {
    return [
      BottomNavigationBarItem(
        icon: Icon(Icons.home),
        label: 'Home',
      ),
      BottomNavigationBarItem(
        icon: Icon(Icons.search),
        label: 'Search',
      ),
      BottomNavigationBarItem(
        icon: Icon(Icons.notifications),
        label: 'Notifications',
      ),
      BottomNavigationBarItem(
        icon: Icon(Icons.person),
        label: 'Profile',
      ),
    ];
  }
}
