package com.ayansh.singtelbillanalyzer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ayansh.CommandExecuter.CommandExecuter;
import com.ayansh.CommandExecuter.Invoker;
import com.ayansh.CommandExecuter.ProgressInfo;
import com.ayansh.CommandExecuter.ResultObject;
import com.ayansh.singtelbillanalyzer.application.Constants;
import com.ayansh.singtelbillanalyzer.application.PhoneBill;
import com.ayansh.singtelbillanalyzer.application.ReadPDFFileCommand;
import com.ayansh.singtelbillanalyzer.application.SBAApplication;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.Iterator;

public class NewBill extends AppCompatActivity implements OnClickListener, Invoker {
	
	private EditText fileName, password;
	private Uri fileURI;
	private ProgressDialog pd;
	private Spinner billType;
	private TextView helpText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.new_bill);

        setTitle("Upload Bill");

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
		
		fileName = (EditText) findViewById(R.id.file_name);
		fileName.setOnClickListener(this);
		
		password = (EditText) findViewById(R.id.pwd);
        password.setVisibility(View.GONE);  // Hide it.
		
		Button uploadButton = (Button) findViewById(R.id.upload);
		uploadButton.setOnClickListener(this);
		
		billType = (Spinner) findViewById(R.id.bill_type);
		
		helpText = (TextView) findViewById(R.id.help_text);
		
		showHelp();
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

	private void showHelp() {
		
		String help = "Please Note : \n" +
                "1. Internet connectivity is required to analyze the bill "
				+ "because this app depends on Google Charts API"
				+ "\n";
		
		help = help + "2. You will need a file browser app to browse files on "
				+ "device and upload. " +
                "If you don't have, we recommend to download ES File Explorer app\n";
		
		help = help + "3. You must download and save the file on your phone. Only then "
				+ "the app can read the file. Files from cache can't be read properly.\n";
		
		if (!Constants.isPremiumVersion()) {
		
			help = help + "4. You are using the free version of the app.\n"
					+ "In this version, you can anlyze only 1 bill at a time.\n"
					+ "Old bill will be deleted.";
		}
		
		helpText.setText(help);
		
	}

	@Override
	public void onClick(View view) {
		
		switch(view.getId()){
			
		case R.id.file_name:
			selectFile();
			break;
			
		case R.id.upload:
			deleteOldBillDetails();
			uploadFile();
			break;
			
		}
		
	}

	private void deleteOldBillDetails() {
		
		if (Constants.isPremiumVersion()) {
			// Premium Version
			return;
		}
		
		SBAApplication app = SBAApplication.getInstance();
		
		Iterator<PhoneBill> i = app.getPhoneBillList(false).iterator();
		
		while(i.hasNext()){
			
			app.deleteBill(i.next().getBillNo());
			
		}
		
	}

	private void selectFile() {

        File file = Environment.getExternalStorageDirectory();

		Intent fileSelect = new Intent(Intent.ACTION_GET_CONTENT);
		fileSelect.setDataAndType(Uri.fromFile(file), "*/*");
		fileSelect.addCategory(Intent.CATEGORY_OPENABLE);

	    try {
	    	
	        startActivityForResult(Intent.createChooser(fileSelect, "Select a File to Upload"),100);
	        
	    } catch (android.content.ActivityNotFoundException ex) {
	        // Potentially direct the user to the Market with a Dialog
	        Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
	    }
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    
		switch (requestCode) {
		
	        case 100:
	        if (resultCode == RESULT_OK) {
	        	
	            // Get the Uri of the selected file 
	            fileURI = data.getData();
	            
	            // Get the path
	            Cursor returnCursor = getContentResolver().query(fileURI, null, null, null, null);
	            
	            if(returnCursor != null){
	            	
	            	int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
		            returnCursor.moveToFirst();
		            
		            String fname = returnCursor.getString(nameIndex);
		            
		            fileName.setText(fname);
	            }
	            
	        }
	        
	        break;
	    }
		
	    super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void uploadFile(){
		
		int bt = billType.getSelectedItemPosition();
		String pwd = password.getEditableText().toString();
		
		String filename = fileName.getEditableText().toString();
		
		CommandExecuter ce = new CommandExecuter();
		
		ReadPDFFileCommand command = new ReadPDFFileCommand(this, bt, filename, fileURI);
		
		command.setPassword(pwd);
		
		pd = ProgressDialog.show(this, "Reading PDF File", "Please wait while we read the PDF File");
		pd.setMax(100);
		
		ce.execute(command);

		// Log Firebase Event
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "bill_upload");
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "bill_upload");
		SBAApplication.getInstance().getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
		
	}

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		pd.dismiss();
		
		if(result.isCommandExecutionSuccess()){
			
			this.setResult(RESULT_OK);
			finish();
			
		}
		else{
			
			Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
			Log.e(SBAApplication.TAG, result.getErrorMessage(), result.getException());
		}
		
	}

	@Override
	public void ProgressUpdate(ProgressInfo pi) {
		
		pd.setProgress(pi.getProgressPercentage());
		pd.setMessage(pi.getProgressMessage());
		
	}
	
}