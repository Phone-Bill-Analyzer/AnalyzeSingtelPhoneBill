package com.ayansh.singtelbillanalyzer;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;

public class SettingsActivity extends PreferenceActivity {

	public static final String INC_INCOMING_CALLS = "include_incoming_calls";
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
                
        addPreferencesFromResource(R.xml.preferences);

        getActionBar().setDisplayHomeAsUpEnabled(true);
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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
	
}