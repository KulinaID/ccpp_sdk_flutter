package id.kulina.ccppsdkflutter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccpp.pgw.sdk.android.callback.TransactionResultCallback;
import com.ccpp.pgw.sdk.android.core.authenticate.PGWJavaScriptInterface;
import com.ccpp.pgw.sdk.android.core.authenticate.PGWWebViewClient;
import com.ccpp.pgw.sdk.android.enums.APIResponseCode;
import com.ccpp.pgw.sdk.android.model.api.response.TransactionResultResponse;

public class WebViewFragment extends Fragment {

  private static final String ARG_REDIRECT_URL = "ARG_REDIRECT_URL";

  private String mRedirectUrl;

  public WebViewFragment() { }

  public static WebViewFragment newInstance(String redirectUrl) {

    WebViewFragment fragment = new WebViewFragment();
    Bundle args = new Bundle();
    args.putString(ARG_REDIRECT_URL, redirectUrl);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if(getArguments() != null) {
      mRedirectUrl = getArguments().getString(ARG_REDIRECT_URL);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    //Authentication handling for 3DS payment
    WebView webview = new WebView(getActivity());
    webview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
    webview.getSettings().setJavaScriptEnabled(true);

    webview.setWebViewClient(new PGWWebViewClient());
    webview.addJavascriptInterface(new PGWJavaScriptInterface(mTransactionResultCallback),
            PGWJavaScriptInterface.JAVASCRIPT_TRANSACTION_RESULT_KEY);
    webview.loadUrl(mRedirectUrl);
    return webview;
  }

  @Override
  public void onDestroyView() {

    getActivity().setResult(Activity.RESULT_CANCELED);
    super.onDestroyView();
  }

  private TransactionResultCallback mTransactionResultCallback = new TransactionResultCallback() {

    @Override
    public void onResponse(TransactionResultResponse response) {

      if(response.getResponseCode().equals(APIResponseCode.TRANSACTION_COMPLETED)) {

        String transactionID = response.getTransactionID();
        Intent result = new Intent();
        result.putExtra("result", transactionID);
        getActivity().setResult(Activity.RESULT_OK, result);
      } else {
        //Get error response and display error
        Intent result = new Intent();
        result.putExtra("result", response.getResponseDescription());
        getActivity().setResult(Activity.RESULT_OK, result);
      }
    }

    @Override
    public void onFailure(Throwable error) {
      //Get error response and display error
      Intent result = new Intent();
      result.putExtra("result", error.getMessage());
      getActivity().setResult(Activity.RESULT_OK, result);
    }
  };
}
