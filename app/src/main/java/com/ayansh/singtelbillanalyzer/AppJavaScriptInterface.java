package com.ayansh.singtelbillanalyzer;

import android.webkit.JavascriptInterface;

import com.ayansh.singtelbillanalyzer.application.PhoneBill;

public class AppJavaScriptInterface {

	private PhoneBill bill;
	
	public AppJavaScriptInterface(PhoneBill bill) {
		this.bill = bill;
	}

	@JavascriptInterface
	public String getTop5ContactsByAmount(){
		
		return bill.getTop5ContactsByAmount().toString();
		
	}
	
	@JavascriptInterface
	public String getSummaryByContactGroups(){
		
		return bill.getSummaryByContactGroups().toString();
		
	}
	
	@JavascriptInterface
	public String getSummaryByContactNames(){
		
		return bill.getSummaryByContactNames().toString();
		
	}
	
}