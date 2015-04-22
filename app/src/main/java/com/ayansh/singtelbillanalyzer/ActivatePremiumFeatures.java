/**
 * 
 */
package com.ayansh.singtelbillanalyzer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ayansh.singtelbillanalyzer.application.Constants;
import com.ayansh.singtelbillanalyzer.application.SBAApplication;
import com.ayansh.singtelbillanalyzer.util.IabHelper;
import com.ayansh.singtelbillanalyzer.util.IabHelper.OnIabPurchaseFinishedListener;
import com.ayansh.singtelbillanalyzer.util.IabResult;
import com.ayansh.singtelbillanalyzer.util.Purchase;
import com.google.android.gms.analytics.GoogleAnalytics;

/**
 * @author varun
 *
 */
public class ActivatePremiumFeatures extends Activity implements
		OnClickListener, OnIabPurchaseFinishedListener {

	private TextView prodName, prodDesc, prodHelp;
	private Button buy, cancel;
	private IabHelper billingHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.premium_features);
				
		prodName = (TextView) findViewById(R.id.product_name);
		prodDesc = (TextView) findViewById(R.id.product_desc);
		prodHelp = (TextView) findViewById(R.id.product_help);
		
		buy = (Button) findViewById(R.id.buy);
		buy.setOnClickListener(this);
		
		cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
				
	}
	
	@Override
	protected void onStart() {
		
		super.onStart();
		
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
		
		billingHelper = IabHelper.getInstance();
		
		if(billingHelper == null){
			finish();
		}
		
		//*
		prodName.setText(Constants.getProductTitle());
		prodDesc.setText(Constants.getProductDescription());
		//*/
		
		buy.setText(Constants.getProductPrice());
		
		showProductHelp();
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
			case android.R.id.home:
				finishActivity(false);
				return true;
				
			default:
                return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	
	    	setResult(RESULT_CANCELED, new Intent());
			finish();
	    	return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	private void showProductHelp() {
		
		prodName.setText("Premium Service");
		
		String desc = "We hope that this application is useful to you.\n"
				+ "Please recommend this application to your friends.\n\n"
				+ "Currently, you have access to free service only. As part of free "
				+ "service, you are allowed to analyze only 1 phone bill\n\n"
				+ "If you want to analyze multiple phone bills, please upgrade to our "
				+ "premium service.";

		prodDesc.setText(desc);
		
		String help = "By purchasing this premium service, all advertisements from the app "
				+ "will be removed permanently.\n"
				+ "Additionally, you can analyze multiple phone bills.";
		
		prodHelp.setText(help);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.buy:
			buy();
			break;

		case R.id.cancel:
			setResult(RESULT_CANCELED, new Intent());
			finish();
			break;
		}
		
	}

	private void buy() {
		
		String devPayLoad = ""; //Application.get_instance().getEmail();
		billingHelper.launchPurchaseFlow(this, Constants.getProductKey(), 22, this, devPayLoad);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (billingHelper.handleActivityResult(requestCode, resultCode, data)) {
			// Nothing
		} else {
			// Handle
		}

	}

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		
		if (result.isFailure()) {
			
			String message = "Error purchasing: " + result.getMessage();
			
			Log.d(SBAApplication.TAG, message);
			
			showAlertDialog(message, false);
			return;
		}
		
		if(info.getSku().contentEquals(Constants.getProductKey())){
			// Purchase was success
			Constants.setPremiumVersion(true);
			
			String message = "Congratulations. You have access to premium service.\n" +
					"Please restart the app to see the changes";
			
			showAlertDialog(message, true);
		}
		
	}

	private void showAlertDialog(String message, final boolean restartApp) {
		
		// Show alert dialog
		new AlertDialog.Builder(this)
			.setTitle("Purchase Status:")
			.setMessage(message)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					// Dismiss the dialog
					dialog.dismiss();
					finishActivity(restartApp);
					
				}
			})
			.show();
		
	}
	
	private void finishActivity(boolean restartApp) {
		
		Intent returnData = new Intent();
		returnData.putExtra("RestartApp", restartApp);
		
		setResult(RESULT_OK, returnData);
		
		// Close this activity
		finish();
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
}