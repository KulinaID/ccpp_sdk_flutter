import 'dart:async';

import 'package:flutter/services.dart';

class CcppSdkFlutter {
  static const MethodChannel _channel = const MethodChannel('ccpp_sdk_flutter');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future initialize(
      String merchantId, ApiEnvironment environment) async {
    await _channel.invokeMethod('initialize',
        {'merchantId': merchantId, 'environment': environment.index});
  }

  static Future<String> paymentWithCreditCard(
      String paymentToken,
      String creditCardNumber,
      int expiryMonth,
      int expiryYear,
      String cvv) async {
    var args = {
      'paymentToken': paymentToken,
      'ccNumber': creditCardNumber,
      'expMonth': expiryMonth,
      'expYear': expiryYear,
      'cvv': cvv
    };
    var transactionId =
        await _channel.invokeMethod('paymentWithCreditCard', args);
    return transactionId;
  }
}

enum ApiEnvironment {
  sandbox,
  production,
  productionID,
}
