import 'package:flutter/material.dart';
import 'package:sonaged/shared/widgets/info_card.dart';
import 'package:sonaged/shared/widgets/side_menu_tile.dart';

class SideMenu extends StatefulWidget {
  const SideMenu({super.key});

  @override
  State<SideMenu> createState() => _SideMenuState();
}

class _SideMenuState extends State<SideMenu> {
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 288,
      height: double.infinity,
      color: const Color(0xFF17203A),
      child: Column(
        children: [
          const InfoCard(
            name: 'My Profile',
            profession: "",
          ),
          Padding(
              padding: const EdgeInsets.only(left: 24, top: 32, bottom: 16),
              child: Text("TEST")),
          const SideMenuTile()
        ],
      ),
    );
  }
}
