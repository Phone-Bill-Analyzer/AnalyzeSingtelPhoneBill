package com.ayansh.singtelbillanalyzer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import com.ayansh.singtelbillanalyzer.application.Constants;
import com.ayansh.singtelbillanalyzer.application.PhoneBill;
import com.ayansh.singtelbillanalyzer.application.SBAApplication;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressLint("SetJavaScriptEnabled")
public class AnaylzeBill extends Activity implements OnItemSelectedListener {

	private PhoneBill bill;
	private WebView webView;
	private String htmlText;
	private Spinner analysisType;
	private ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bill_analysis);
		
		setTitle("Analyze Bill");
				
		// Show Ads
		if (!Constants.isPremiumVersion()) {

			// Show Ad.
			AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice("9F11CAC92EB404500CAA3F8B0BBA5277").build();
			
			AdView adView = (AdView) findViewById(R.id.adView);

			// Start loading the ad in the background.
			adView.loadAd(adRequest);
		}
		
		int pos = getIntent().getIntExtra("Position", -1);
		if(pos < 0){
			return;
		}
		
		analysisType = (Spinner) findViewById(R.id.analysis_type);
		analysisType.setOnItemSelectedListener(this);
		
		bill = SBAApplication.getInstance().getPhoneBillList(false).get(pos);
		
		webView = (WebView) findViewById(R.id.webview);
		
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		webView.addJavascriptInterface(new AppJavaScriptInterface(bill), "App");
		
		webView.setWebViewClient(new myWebViewClient());
		
	}
	
	@Override
	protected void onStart(){
		
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	@Override
	protected void onStop(){
		
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	private void showFromRawSource() {
		
		pd = ProgressDialog.show(this, "Loading...", "Please wait while we load the chart");
		
		//webView.clearCache(true);
		
		webView.loadData(htmlText, "text/html", "utf-8");
		//webView.loadDataWithBaseURL( "file:///android_asset/", htmlText, "text/html", "utf-8", null );
		//webView.loadDataWithBaseURL( "file:///android_asset/", htmlText, "text/html", "utf-8", "");

	}
	
	private void getHTMLFromFile(String fileName) {
		// Get HTML File from RAW Resource
		Resources res = getResources();
        InputStream is;
        
		try {
			
			is = res.getAssets().open(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        htmlText = "";
	        String line = "";
	        
	        while((line = reader.readLine()) != null){
				htmlText = htmlText + "\n" + line;
			}
	        
		} catch (IOException e) {
			Log.e(SBAApplication.TAG, e.getMessage(), e);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		
		switch(pos){
			
		case 0:
			getHTMLFromFile("all_contacts_table.html");
			break;
			
		case 1:
			
			getHTMLFromFile("top_5_pie_chart.html");
			break;
			
		case 2:
			getHTMLFromFile("contact_group_summary.html");
			break;

        case 3:
            getHTMLFromFile("contacts_without_names.html");
            break;

        case 4:
            getHTMLFromFile("contacts_without_groups.html");
            break;

		}
		
		showFromRawSource();
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
		getHTMLFromFile("all_contacts_table.html");
		showFromRawSource();
	}
	
	private class myWebViewClient extends WebViewClient{
		
		@Override
		public void onPageFinished(WebView view, String url){
			
			if(pd != null && pd.isShowing()){
				pd.dismiss();
			}
			
			super.onPageFinished(view, url);
			
		}
	}
	
}