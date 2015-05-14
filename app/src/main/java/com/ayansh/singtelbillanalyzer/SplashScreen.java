package com.ayansh.singtelbillanalyzer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ayansh.singtelbillanalyzer.application.Constants;
import com.ayansh.singtelbillanalyzer.application.SBAApplication;
import com.ayansh.singtelbillanalyzer.util.IabHelper;
import com.ayansh.singtelbillanalyzer.util.IabResult;
import com.ayansh.singtelbillanalyzer.util.Inventory;
import com.ayansh.singtelbillanalyzer.util.Purchase;
import com.google.android.gms.analytics.GoogleAnalytics;

import java.util.ArrayList;
import java.util.List;


public class SplashScreen extends Activity implements IabHelper.OnIabSetupFinishedListener, IabHelper.QueryInventoryFinishedListener {

    private IabHelper billingHelper;

    private TextView statusView;
    private boolean appStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        TextView versionInfo = (TextView) findViewById(R.id.version);

        try{
            String versionName = "Version: ";
            versionName += getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionInfo.setText(versionName);
        }catch(Exception e){
            // Ignore error.
        }

        statusView = (TextView) findViewById(R.id.status);
        statusView.setText("Initializing");

        SBAApplication app = SBAApplication.getInstance();

        app.setContext(getApplicationContext());

        if (!app.isEULAAccepted()) {

            Intent eula = new Intent(SplashScreen.this, Eula.class);
            eula.putExtra("File", "eula.html");
            eula.putExtra("Title", "End User License Agreement: ");
            SplashScreen.this.startActivityForResult(eula, 100);

        } else {
            // Start the Main Activity
            startSplashScreenActivity();
        }

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

    private void startSplashScreenActivity() {

        // Instantiate billing helper class
        billingHelper = IabHelper.getInstance(this, Constants.getPublicKey());

        if(billingHelper.isSetupComplete()){
            // Set up is already done... so Initialize app.
            startApp();
        }
        else{
            // Set up
            try{
                billingHelper.startSetup(this);
            }
            catch(Exception e){
                // Oh Fuck !
                Log.w(SBAApplication.TAG, e.getMessage(), e);
                billingHelper.dispose();
                finish();
            }
        }

    }

    private void startApp() {

        SBAApplication app = SBAApplication.getInstance();

        if(appStarted){
            return;
        }

        // Show help for the 1st launch
        if(app.getOptions().get("FirstLaunch") == null){
            // This is first launch !
            showHelp();
            return;
        }

        // Check if version is updated.
        int oldAppVersion = app.getOldAppVersion();
        int newAppVersion;
        try {
            newAppVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            newAppVersion = 0;
            Log.e(SBAApplication.TAG, e.getMessage(), e);
        }

        if(newAppVersion > oldAppVersion ){
            // Update App Version
            app.updateVersion();

            showWhatsNew();
            return;
        }

        appStarted = true;

        // Start the Main
        Log.i(SBAApplication.TAG, "Start Main");
        Intent start = new Intent(SplashScreen.this, Main.class);
        SplashScreen.this.startActivity(start);

        // Kill this activity.
        Log.i(SBAApplication.TAG, "Kill Splash screen");
        SplashScreen.this.finish();

    }

    @Override
    public void onIabSetupFinished(IabResult result) {

        if (!result.isSuccess()) {

            // Log error ! Now I don't know what to do
            Log.w(SBAApplication.TAG, result.getMessage());

            Constants.setPremiumVersion(false);

            // Initialize the app
            startApp();


        } else {

            // Check if the user has purchased premium service
            // Query for Product Details

            List<String> productList = new ArrayList<String>();
            productList.add(Constants.getProductKey());

            try{
                billingHelper.queryInventoryAsync(true, productList, this);
            }
            catch(Exception e){
                Log.w(SBAApplication.TAG, e.getMessage(), e);
            }

        }

    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inv) {

        if (result.isFailure()) {

            // Log error ! Now I don't know what to do
            Log.w(SBAApplication.TAG, result.getMessage());

            Constants.setPremiumVersion(false);

        } else {

            String productKey = Constants.getProductKey();

            Purchase item = inv.getPurchase(productKey);

            if (item != null) {
                // Has user purchased this premium service ???
                Constants.setPremiumVersion(inv.hasPurchase(productKey));

            }
            else{
                Constants.setPremiumVersion(false);
            }

            if(inv.getSkuDetails(productKey) != null){

                Constants.setProductTitle(inv.getSkuDetails(productKey).getTitle());
                Constants.setProductDescription(inv.getSkuDetails(productKey).getDescription());
                Constants.setProductPrice(inv.getSkuDetails(productKey).getPrice());
            }

        }

        // Initialize the app
        startApp();

    }

    private void showWhatsNew() {

        Intent newFeatures = new Intent(SplashScreen.this, DisplayFile.class);
        newFeatures.putExtra("File", "NewFeatures.html");
        newFeatures.putExtra("Title", "New Features: ");
        SplashScreen.this.startActivityForResult(newFeatures, 901);
    }

    private void showHelp() {

        Intent help = new Intent(SplashScreen.this, DisplayFile.class);
        help.putExtra("File", "help.html");
        help.putExtra("Title", "Help: ");
        SplashScreen.this.startActivityForResult(help, 900);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        SBAApplication app = SBAApplication.getInstance();

        switch (requestCode) {

            case 100:
                if (!app.isEULAAccepted()) {
                    finish();
                } else {
                    // Start Main Activity
                    startSplashScreenActivity();
                }
                break;

            case 900:
                app.addParameter("FirstLaunch", "Completed");
                startApp();
                break;

            case 901:
                startApp();
                break;
        }
    }

}
