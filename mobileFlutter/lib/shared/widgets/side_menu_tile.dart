import 'package:flutter/material.dart';

class SideMenuTile extends StatelessWidget {
  const SideMenuTile({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        const Padding(
          padding: EdgeInsets.only(left: 24),
          child: Divider(
            color: Colors.white24,
            height: 1,
          ),
        ),
        ListTile(
          onTap: () => {},
          leading: const SizedBox(
            height: 34,
            width: 34,
            child: Icon(
              Icons.home,
              size: 40,
            ),
            //RiveAnimation.asset(asset,artboard: "Home", onInit(artboard){}),
          ),
          title: const Text(
            "Accueil",
            style: TextStyle(),
          ),
        ),
        ListTile(
          onTap: () => {},
          leading: const SizedBox(
            height: 34,
            width: 34,
            child: Icon(
              Icons.settings,
              size: 40,
            ),
            //RiveAnimation.asset(asset,artboard: "Home", onInit(artboard){}),
          ),
          title: const Text(
            "Paramétres",
            style: TextStyle(),
          ),
        ),
        ListTile(
          onTap: () => {},
          leading: const SizedBox(
            height: 34,
            width: 34,
            child: Icon(
              Icons.notifications,
              size: 40,
            ),
            //RiveAnimation.asset(asset,artboard: "Home", onInit(artboard){}),
          ),
          title: const Text(
            "Notifications",
            style: TextStyle(),
          ),
        ),
        ListTile(
          onTap: () => {},
          leading: const SizedBox(
            height: 45,
            width: 45,
            child: Icon(
              Icons.exit_to_app,
              size: 40,
            ),
            //RiveAnimation.asset(asset,artboard: "Home", onInit(artboard){}),
          ),
          title: const Text(
            "Déconnexion",
            style: TextStyle(),
          ),
        )
      ],
    );
  }
}
