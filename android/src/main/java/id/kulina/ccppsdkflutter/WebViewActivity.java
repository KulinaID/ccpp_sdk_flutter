package id.kulina.ccppsdkflutter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.support.v7.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        String redirect = i.getStringExtra("redirect");

        WebViewFragment fragment = WebViewFragment.newInstance(redirect);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, "").commit();
    }

}
