import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:ccpp_sdk_flutter/ccpp_sdk_flutter.dart';

void main() async {
  await CcppSdkFlutter.initialize("JT01", ApiEnvironment.sandbox);
  runApp(new MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await CcppSdkFlutter.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: new Center(
          child: Column(
            children: <Widget>[
              new Text('Running on: $_platformVersion\n'),
              RaisedButton(
                child: Text('pay'),
                onPressed: () async {
                  var s = await CcppSdkFlutter.paymentWithCreditCard(
                      "adad", "creditCardNumber", 1, 2011, "cvv");
                  print(s);
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
