package com.ayansh.singtelbillanalyzer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ayansh.CommandExecuter.CommandExecuter;
import com.ayansh.CommandExecuter.Invoker;
import com.ayansh.CommandExecuter.ProgressInfo;
import com.ayansh.CommandExecuter.ResultObject;
import com.ayansh.singtelbillanalyzer.application.Constants;
import com.ayansh.singtelbillanalyzer.application.SBAApplication;
import com.ayansh.singtelbillanalyzer.application.WebSyncCommand;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by varun on 5/14/15.
 */
public class WebSync extends AppCompatActivity implements View.OnClickListener, Invoker {

    private EditText sessionID;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.web_sync);

        setTitle("Analyze on Web");

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

        sessionID = (EditText) findViewById(R.id.session_id);

        Button syncButton = (Button) findViewById(R.id.web_sync);
        syncButton.setOnClickListener(this);

        String helpText = "<html>After synchronization, visit our " +
                "<a href=\"http://apps.ayansh.com/Phone-Bill-Analyzer/analyze_bill.php\">Web App</a>" +
                " for detailed analysis</html>";

        WebView wv = (WebView) findViewById(R.id.help_text);
        wv.loadData(helpText, "text/html", "utf-8");

        wv.setOnClickListener(this);

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
    public void NotifyCommandExecuted(ResultObject result) {

        pd.dismiss();

        if(result.isCommandExecutionSuccess()){
            Toast.makeText(this, "Sync successful.", Toast.LENGTH_LONG).show();
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

    @Override
    public void onClick(View view) {

        switch(view.getId()){

            case R.id.web_sync:
                webSync();
                break;

            case R.id.help_text:
                Intent webAnalysis = new Intent(Intent.ACTION_VIEW, Uri.parse("http://apps.ayansh.com/Phone-Bill-Analyzer/analyze_bill.php"));
                startActivity(webAnalysis);
                break;

        }
    }

    private void webSync() {

        String sid = sessionID.getEditableText().toString();

        CommandExecuter ce = new CommandExecuter();

        WebSyncCommand command = new WebSyncCommand(this, sid);

        pd = ProgressDialog.show(this, "Synchronizing...", "Please wait while we synchronize with your web session");
        pd.setMax(100);

        ce.execute(command);

        // Log Firebase Event
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "web_sync");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "web_sync");
        SBAApplication.getInstance().getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

    }
}
