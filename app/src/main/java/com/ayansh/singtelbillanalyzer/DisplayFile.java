package com.ayansh.singtelbillanalyzer;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.WebView;

import com.ayansh.singtelbillanalyzer.application.Constants;
import com.ayansh.singtelbillanalyzer.application.SBAApplication;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DisplayFile extends AppCompatActivity {
	
	private String html_text;
	private WebView my_web_view;
	private boolean show_ad = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);       
        setContentView(R.layout.file_display);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        my_web_view = (WebView) findViewById(R.id.webview);
                
        String title = this.getIntent().getStringExtra("Title");
        if(title == null || title.contentEquals("")){
        	title = getResources().getString(R.string.app_name);
        }
        
        this.setTitle(title);

       	String fileName = getIntent().getStringExtra("File");
       	
       	if(fileName != null){
       		// If File name was provided, show from file name.
       		getHTMLFromFile(fileName);

			if(fileName.contains("about")){
				show_ad = true;
			}

			// Log Firebase Event
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, fileName);
			bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "display_file");
			SBAApplication.getInstance().getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

       	}
       	else{
       		// Else, show data directly.
       		String subject = getIntent().getStringExtra("Subject");
       		String content = getIntent().getStringExtra("Content");
       		html_text = "<html><body>" +
       				"<h3>" + subject + "</h3>" +
       				"<p>" + content + "</p>" +
       				"</body></html>";
       	}
       	
       	showFromRawSource();
       	
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
	
	@Override
	protected void onDestroy(){

		if(show_ad){
			showInterstitialAd();
		}

		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	        setResult(RESULT_CANCELED, null);
	    }

	    return super.onKeyDown(keyCode, event);
	}

	private void showInterstitialAd(){

		if (!Constants.isPremiumVersion()) {

			InterstitialAd iad = MyInterstitialAd.getInterstitialAd(this);
			if(iad.isLoaded()){
				iad.show();
			}
		}

	}
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}

	private void showFromRawSource() {
		my_web_view.clearCache(true);
        my_web_view.setBackgroundColor(Color.TRANSPARENT);
        //my_web_view.setBackgroundResource(R.drawable.background);
        my_web_view.loadData(html_text, "text/html", "utf-8");
	}

	private void getHTMLFromFile(String fileName) {
		// Get HTML File from RAW Resource
		Resources res = getResources();
        InputStream is;
        
		try {
			
			is = res.getAssets().open(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        html_text = "";
	        String line = "";
	        
	        while((line = reader.readLine()) != null){
				html_text = html_text + line;
			}
	        
		} catch (IOException e) {
			Log.e(SBAApplication.TAG, e.getMessage(), e);
		}
	}

}