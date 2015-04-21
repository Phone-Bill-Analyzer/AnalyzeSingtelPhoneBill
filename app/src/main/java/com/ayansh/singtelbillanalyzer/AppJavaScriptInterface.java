package com.ayansh.singtelbillanalyzer;

import android.webkit.JavascriptInterface;

import com.ayansh.singtelbillanalyzer.application.PhoneBill;
import com.ayansh.singtelbillanalyzer.application.SBAApplication;

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

    @JavascriptInterface
    public String getContactsWithoutNames(){

        return bill.getContactsWithoutNames().toString();

    }

    @JavascriptInterface
    public String getContactsWithoutGroups(){

        return bill.getContactsWithoutGroups().toString();

    }

    @JavascriptInterface
    public String compareMonthlyUsage(){

        return SBAApplication.getInstance().getMonthlyComparision().toString();

    }

}