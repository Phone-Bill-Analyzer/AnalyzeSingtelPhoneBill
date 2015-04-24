package com.ayansh.singtelbillanalyzer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.ayansh.singtelbillanalyzer.application.Constants;
import com.ayansh.singtelbillanalyzer.application.PhoneBill;
import com.ayansh.singtelbillanalyzer.application.ReloadContactsInfoCommand;
import com.ayansh.singtelbillanalyzer.application.SBAApplication;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;

import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import java.util.ArrayList;
import java.util.List;

public class Main extends Activity implements OnItemClickListener, Invoker {
	
	private ListView listView;
	private BillListAdapter adapter;
	private List<PhoneBill> billList;
	private ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		setTitle("Bill List");
		
		SBAApplication.getInstance().setContext(getApplicationContext());
		
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
		
		// Initialize Preferences
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		billList = new ArrayList<PhoneBill>();
		
		billList.addAll(SBAApplication.getInstance().getPhoneBillList(true));
		
		billList.add(0, new PhoneBill("DUMMY"));	// Dummy Entry
		
		listView = (ListView) findViewById(R.id.bill_list);
		
		adapter = new BillListAdapter(this, R.layout.billlistrow, R.id.phone_no, billList);
		
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		listView.setOnItemClickListener(this);
		
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

        if (!Constants.isPremiumVersion()) {
            menu.findItem(R.id.Analyze).setVisible(false);
            menu.findItem(R.id.DownloadCSV).setVisible(false);
        }

        if(billList.size() > 1 && billList.get(1).getPhoneNumber().contentEquals("81277490")){
            menu.findItem(R.id.DownloaDB).setVisible(true);
            menu.findItem(R.id.Analyze).setVisible(true);
            menu.findItem(R.id.DownloadCSV).setVisible(true);
        }
        else{
            menu.findItem(R.id.DownloaDB).setVisible(false);
        }

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()){
		
		case R.id.settings:
			Intent settings = new Intent(Main.this, SettingsActivity.class);
			Main.this.startActivity(settings);
			
			break;
			
		case R.id.Help:
			Intent help = new Intent(Main.this, DisplayFile.class);
			help.putExtra("File", "help.html");
			help.putExtra("Title", "Help: ");
			Main.this.startActivity(help);
			break;
			
		case R.id.About:
    		Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			Main.this.startActivity(info);
			break;
			
		case R.id.ReloadContacts:

			CommandExecuter ce = new CommandExecuter();
			ReloadContactsInfoCommand command = new ReloadContactsInfoCommand(this);
			pd = ProgressDialog.show(this, "Please wait", "Re-loading contacts information");
			ce.execute(command);
			break;

		case R.id.DownloaDB:
			SBAApplication.getInstance().downloaDBData();
            Toast.makeText(this,"DB File Downloaded",Toast.LENGTH_LONG).show();
			break;

        case R.id.DownloadCSV:
            SBAApplication.getInstance().downloadCSVData();
            Toast.makeText(this,"CSV File Downloaded in folder: Android/Data",Toast.LENGTH_LONG).show();
            break;

        case R.id.Analyze:
            compareBills();
            break;

		}
		
		return true;
	}

    private void compareBills(){

        Intent compareBills = new Intent(Main.this, CompareBills.class);
        Main.this.startActivity(compareBills);

    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		
		if (pos == 0) {

			if (billList.size() >= 2 && !Constants.isPremiumVersion()) {

				Intent buy = new Intent(Main.this, ActivatePremiumFeatures.class);
				Main.this.startActivityForResult(buy, 900);
				
			} else {
				// New Phone Bill
				Intent newBill = new Intent(Main.this, NewBill.class);
				Main.this.startActivityForResult(newBill, 100);
			}
			
		} else {

			Intent analyzeBill = new Intent(Main.this, AnaylzeBill.class);
			analyzeBill.putExtra("Position", pos - 1);
			Main.this.startActivity(analyzeBill);
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case 100:
			
			billList.clear();
			billList.addAll(SBAApplication.getInstance().getPhoneBillList(true));
			billList.add(0, new PhoneBill("DUMMY"));	// Dummy Entry
			
			adapter.notifyDataSetChanged();
			break;
			
		case 900:
			
			if(resultCode == RESULT_OK){
				
				if (data.getBooleanExtra("RestartApp", false)) {
					finish();
				}
			}
			else{
				
				// New Phone Bill
				Intent newBill = new Intent(Main.this, NewBill.class);
				Main.this.startActivityForResult(newBill, 100);
			}
			
			break;
			
		}
	}

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		pd.dismiss();
		
		if(result.isCommandExecutionSuccess()){
			Toast.makeText(this, "Contacts Reloaded successfuly", Toast.LENGTH_LONG).show();
		}
		else{
			Toast.makeText(this, "Error occured while loading contacts info", Toast.LENGTH_LONG).show();
			Log.e(SBAApplication.TAG, result.getErrorMessage(), result.getException());
		}
		
	}

	@Override
	public void ProgressUpdate(ProgressInfo pi) {
		// Nothing to do.
	}

}
