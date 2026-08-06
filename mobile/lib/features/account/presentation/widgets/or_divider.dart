import 'package:flutter/material.dart';
import 'package:sonaged/configs/constants/style_constant.dart';

class OrDivider extends StatelessWidget {
  const OrDivider({super.key});

  @override
  Widget build(BuildContext context) {
    Size size = MediaQuery.of(context).size;
    return Container(
      margin: EdgeInsets.symmetric(vertical: size.height * 0.02),
      width: size.width * 0.8,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          buildDivider(size),
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 10),
            child: Text(
              "ou",
              textAlign: TextAlign.center,
              style: TextStyle(
                color: AppTextStyles.kTextFieldFill,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          buildDivider(size),
        ],
      ),
    );
  }

  Widget buildDivider(Size size) {
    return SizedBox(
      width: size.width * 0.2,
      child: const Divider(
        color: Color(0xFFD9D9D9),
        height: 1.5,
      ),
    );
  }
}
