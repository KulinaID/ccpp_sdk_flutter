import Flutter
import UIKit
import WebKit
import PGW

public class SwiftCcppSdkFlutterPlugin: NSObject, FlutterPlugin, UINavigationControllerDelegate {
  
  fileprivate var result: FlutterResult?
  fileprivate var viewController: UIViewController?
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "ccpp_sdk_flutter", binaryMessenger: registrar.messenger())
    let vc = UIApplication.shared.delegate?.window??.rootViewController
    let instance = SwiftCcppSdkFlutterPlugin(viewController: vc)
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public init(viewController: UIViewController?) {
    self.viewController = viewController
    super.init()
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    self.result = result
    switch(call.method){
    case "getPlatformVersion":
        result("iOS " + UIDevice.current.systemVersion)
    case "initialize":
        var args = call.arguments as! Dictionary<String, Any>
        let merchantId = args["merchantId"] as! String
        let environment = args["environment"] as! Int
        let apiEnv = APIEnvironment.init(rawValue: environment)
        PGWSDK.builder().merchantID(merchantId).apiEnvironment(apiEnv!).initialize();
        result(nil)
    case "paymentWithCreditCard":
        var args = call.arguments as! Dictionary<String, Any>
        let paymentToken = args["paymentToken"] as! String
        let ccNumber = args["ccNumber"] as! String
        let expMonth = args["expMonth"] as! Int
        let expYear = args["expYear"] as! Int
        let cvv = args["cvv"] as! String
        paymentWithCreditCard(paymentToken, ccNumber: ccNumber, expMonth: expMonth, expYear: expYear, cvv: cvv)
    default:
        result(FlutterMethodNotImplemented)
    }
  }
    
    fileprivate func paymentWithCreditCard(_ paymentToken: String, ccNumber: String, expMonth: Int, expYear: Int, cvv: String){
        let paymentToken:String = "roZG9I1hk/GYjNt+BYPYbxQtKElbZDs9M5cXuEbE+Z0QTr/yUcl1oG7t0AGoOJlBhzeyBtf5mQi1UqGbjC66E85S4m63CfV/awwNbbLbkxsvfgzn0KSv7JzH3gcs/OIL"
        
        //Construct credit card request
        let creditCardPayment:CreditCardPayment = CreditCardPaymentBuilder(pan: "4111111111111111")
            .expiryMonth(12)
            .expiryYear(2019)
            .securityCode("123")
            .build()
        
        //Construct transaction request
        let transactionRequest:TransactionRequest = TransactionRequestBuilder(paymentToken: paymentToken)
            .withCreditCardPayment(creditCardPayment)
            .build()
        
        //Execute payment request
        PGWSDK.shared.proceedTransaction(transactionRequest: transactionRequest,
         success: { (response:TransactionResultResponse) in
            
            //For 3DS
            if response.responseCode == APIResponseCode.TRANSACTION_AUTHENTICATE {
                
                let redirectUrl:String = response.redirectUrl!
//                                                self.openWebViewController(redirectUrl) //Open WebView for 3DS
            } else if response.responseCode == APIResponseCode.TRANSACTION_COMPLETED {
                
                //Inquiry payment result by using transaction id.
                let transactionID:String = response.transactionID!
                self.result!(transactionID)
            } else {
                //Get error response and display error
//                self.result!("ERROR" + response.responseDescription!)
              let webView = WKWebViewController()
              webView.redirectUrl = "https://google.com"
              let nav = UINavigationController.init(rootViewController: webView)
              self.viewController?.present(nav, animated: true, completion: nil)
            }
        }) { (error:NSError) in
            //Get error response and display error
          self.result!("ERROR" + error.description)
        }
    }
}

//For WKWebView implementation
class WKWebViewController: UIViewController {
  
  var webView:WKWebView!
  var pgwWebViewDelegate:PGWWKWebViewDelegate!
  var redirectUrl:String?
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    //Authentication handling for 3DS payment
    let requestUrl:URL = URL.init(string: self.redirectUrl!)!
    let request:URLRequest = URLRequest.init(url: requestUrl)
    
    let webConfiguration = WKWebViewConfiguration()
    self.webView = WKWebView(frame: UIScreen.main.bounds, configuration: webConfiguration)
    self.webView.navigationDelegate = self.transactionResultCallback()
    self.webView.load(request)
    
    self.view.addSubview(self.webView)
  }
  
  func transactionResultCallback() -> PGWWKWebViewDelegate {
    
    self.pgwWebViewDelegate = PGWWKWebViewDelegate(
      success: { (response: TransactionResultResponse) in
        
        if response.responseCode == APIResponseCode.TRANSACTION_COMPLETED {
          
          //Inquiry payment result by using transaction id.
          let transactionID:String = response.transactionID!
        } else {
          //Get error response and display error
        }
    }, failure: { (error: NSError) in
      //Get error response and display error
    })
    
    return self.pgwWebViewDelegate
  }
}
