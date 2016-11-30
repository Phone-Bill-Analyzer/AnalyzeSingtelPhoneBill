package com.ayansh.singtelbillanalyzer.application;

import android.net.Uri;

import com.ayansh.CommandExecuter.Command;
import com.ayansh.CommandExecuter.Invoker;
import com.ayansh.CommandExecuter.ProgressInfo;
import com.ayansh.CommandExecuter.ResultObject;

import java.util.ArrayList;

public class ReadPDFFileCommand extends Command {

	private Uri fileUri;
	private String password, fileName;
	private int billType;
	private PhoneBill bill;
	
	public ReadPDFFileCommand(Invoker caller, int bt, String file, Uri uri) {
		
		super(caller);
		fileUri = uri;
		fileName = file;
		billType = bt;
	}

	public void setPassword(String pwd){
		password = pwd;
	}
	
	@Override
	protected void execute(ResultObject result) throws Exception {
		
		String type = "";
		
		bill = new PhoneBill(fileName, fileUri);		
		bill.setPassword(password);
		
		switch(billType){
		
		case 0:
			type = "STPPM";
			break;

        default:
            type = "STPPM";
            break;
		
		}
		
		bill.setBillType(type);
		
		ProgressInfo pi;
		
		// Read PDF File
		bill.readPDFFile();
		
		// Parse the Text and read bill details
		pi = new ProgressInfo(40, "Reading bill details");
		publishProgress(pi);
		
		// Save Bill
		pi = new ProgressInfo(80, "Saving bill details for analysis");
		publishProgress(pi);
		bill.saveToDB();
		
		// Map Contacts and Groups
		mapContactData();
		
	}

	private void mapContactData() {
		
		// Get Distinct Phone Numbers
		ArrayList<String> phoneList = SBAApplicationDB.getInstance().getDistinctPhoneNumbers(bill.getBillNo());
		
		SBAApplication.getInstance().reloadContactsInfo(phoneList);
		
	}

}
