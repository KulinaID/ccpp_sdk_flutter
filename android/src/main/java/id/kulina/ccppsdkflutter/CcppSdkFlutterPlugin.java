package id.kulina.ccppsdkflutter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.ccpp.pgw.sdk.android.builder.CreditCardPaymentBuilder;
import com.ccpp.pgw.sdk.android.builder.TransactionRequestBuilder;
import com.ccpp.pgw.sdk.android.callback.TransactionResultCallback;
import com.ccpp.pgw.sdk.android.core.PGWSDK;
import com.ccpp.pgw.sdk.android.core.authenticate.PGWJavaScriptInterface;
import com.ccpp.pgw.sdk.android.core.authenticate.PGWWebViewClient;
import com.ccpp.pgw.sdk.android.enums.APIEnvironment;
import com.ccpp.pgw.sdk.android.enums.APIResponseCode;
import com.ccpp.pgw.sdk.android.model.api.request.TransactionRequest;
import com.ccpp.pgw.sdk.android.model.api.response.TransactionResultResponse;
import com.ccpp.pgw.sdk.android.model.payment.CreditCardPayment;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** CcppSdkFlutterPlugin */
public class CcppSdkFlutterPlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener {
  private final Activity activity;
  private Result result;
  static final int CCPP_AUTH_REQUEST_CODE = 31;


  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "ccpp_sdk_flutter");
    CcppSdkFlutterPlugin plugin = new CcppSdkFlutterPlugin(registrar.activity());
    registrar.addActivityResultListener(plugin);
    channel.setMethodCallHandler(plugin);
  }

  private CcppSdkFlutterPlugin(Activity activity){
    this.activity = activity;
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    result = result;
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE + " " + APIEnvironment.SANDBOX.ordinal()
      + APIEnvironment.PRODUCTION.ordinal() + APIEnvironment.PRODUCTION_INDONESIA.ordinal());
    } else if (call.method.equals("initialize")){
      String merchantId = call.argument("merchantId");
      APIEnvironment environment = APIEnvironment.values()[(Integer)call.argument("environment")];
      PGWSDK.builder(activity)
              .setMerchantID(merchantId)
              .setAPIEnvironment(environment)
              .init();
      result.success(null);
    } else if (call.method.equals("paymentWithCreditCard")){
      String paymentToken = call.argument("paymentToken");
      String ccNumber = call.argument("ccNumber");
      Integer expMonth = call.argument("expMonth");
      Integer expYear = call.argument("expYear");
      String cvv = call.argument("cvv");
      paymentWithCC(result, paymentToken, ccNumber, expMonth, expYear, cvv);
    } else {
      result.notImplemented();
    }
  }

  private void paymentWithCC(final Result result, String paymentToken, String ccNumber, Integer expMonth, Integer expYear, String cvv) {
    //Construct credit card request
    CreditCardPayment creditCardPayment = new CreditCardPaymentBuilder(ccNumber)
            .setExpiryMonth(expMonth)
            .setExpiryYear(expYear)
            .setSecurityCode(cvv)
            .build();

    //Construct transaction request
    TransactionRequest transactionRequest = new TransactionRequestBuilder(paymentToken)
            .withCreditCardPayment(creditCardPayment)
            .build();

    //Execute payment request
    PGWSDK.getInstance().proceedTransaction(transactionRequest, new TransactionResultCallback() {

      @Override
      public void onResponse(TransactionResultResponse response) {

        //For 3DS
        if(response.getResponseCode().equals(APIResponseCode.TRANSACTION_AUTHENTICATE)) {

					String redirectUrl = response.getRedirectUrl();
					Intent i = new Intent(activity, WebViewActivity.class);
					i.putExtra("redirect", redirectUrl);
					activity.startActivityForResult(i, CCPP_AUTH_REQUEST_CODE); //Open WebView for 3DS
        } else if(response.getResponseCode().equals(APIResponseCode.TRANSACTION_COMPLETED)) {

          //Inquiry payment result by using transaction id.
          String transactionID = response.getTransactionID();
          result.success(transactionID);
        } else {
          //Get error response and display error
          result.success("ERROR " + response.getResponseDescription());
        }
      }

      @Override
      public void onFailure(Throwable error) {
        //Get error response and display error
        result.success("ERROR " + error.getMessage());
      }
    });
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
    if(requestCode == CCPP_AUTH_REQUEST_CODE){
      if(resultCode == Activity.RESULT_OK){
        String res = intent.getStringExtra("result");
        result.success(res);
        return true;
      }
      return true;
    }
    return false;
  }
}


