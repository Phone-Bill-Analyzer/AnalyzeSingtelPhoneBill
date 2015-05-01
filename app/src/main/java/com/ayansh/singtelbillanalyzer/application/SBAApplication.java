package com.ayansh.singtelbillanalyzer.application;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import com.ayansh.singtelbillanalyzer.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by varun on 3/17/15.
 */
public class SBAApplication {

    private static SBAApplication instance;

    public final static String SenderId = "492119277184";
    public static final String TAG = "SBA";

    private Context context;
    private SBAApplicationDB appDB;

    HashMap<String, String> Options;
    private ArrayList<PhoneBill> phoneBillList;

    private SBAApplication(){
        Options = new HashMap<String, String>();
    }

    public static SBAApplication getInstance(){

        if(instance == null){

            instance = new SBAApplication();

        }

        return instance;
    }

    public void setContext(Context c){

        if (context == null) {

            context = c;

            // Initialize the DB.
            appDB = SBAApplicationDB.getInstance(context);
            appDB.openDBForWriting();
            appDB.loadOptions();

            // TODO Initialize Google Analytics
        }

    }

    public Context getContext() {
        return context;
    }

    // Get all Options
    public HashMap<String, String> getOptions() {
        return Options;
    }

    boolean includeIncomingCalls(){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean include = sharedPref.getBoolean(SettingsActivity.INC_INCOMING_CALLS, true);

        return include;

    }

    public void deleteBill(String billNo) {

        List<String> queries = new ArrayList<String>();

        String query = "DELETE FROM BillMetaData WHERE BillNo = '" + billNo + "'";
        queries.add(query);

        query = "DELETE FROM BillCallDetails WHERE BillNo = '" + billNo + "'";
        queries.add(query);

        appDB.executeQueries(queries);

    }

    public boolean isEULAAccepted() {

        String eula = Options.get("EULA");
        if (eula == null || eula.contentEquals("")) {
            eula = "false";
        }
        return Boolean.valueOf(Options.get("EULA"));
    }

    public void setEULAResult(boolean result) {
        // Save EULA Result
        addParameter("EULA", String.valueOf(result));
    }

    public void close() {
        appDB.close();
        context = null;
    }

    // Add parameter
    public boolean addParameter(String paramName, String paramValue) {

        List<String> queries = new ArrayList<String>();
        String query = "";

        if (Options.containsKey(paramName)) {
            // Already exists. Update it.
            query = "UPDATE Options SET ParamValue = '" + paramValue
                    + "' WHERE ParamName = '" + paramName + "'";
        } else {
            // New entry. Create it
            query = "INSERT INTO Options (ParamName, ParamValue) VALUES ('"
                    + paramName + "','" + paramValue + "')";
        }

        queries.add(query);
        boolean success = appDB.executeQueries(queries);

        if (success) {
            Options.put(paramName, paramValue);
        }

        return success;

    }

    public boolean removeParameter(String paramName) {

        List<String> queries = new ArrayList<String>();

        String query = "DELETE FROM Options WHERE ParamName = '" + paramName
                + "'";
        queries.add(query);
        boolean success = appDB.executeQueries(queries);

        if (success) {
            Options.remove(paramName);
        }

        return success;
    }

    public int getOldAppVersion() {
        String versionCode = Options.get("AppVersionCode");
        if (versionCode == null || versionCode.contentEquals("")) {
            versionCode = "0";
        }
        return Integer.valueOf(versionCode);
    }

    public void updateVersion() {
        // Update Version

        int version;
        try {
            version = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            version = 0;
            Log.e(TAG, e.getMessage(), e);
        }

        addParameter("AppVersionCode", String.valueOf(version));
    }

    public ArrayList<PhoneBill> getPhoneBillList(boolean reload) {

        if(reload){
            phoneBillList = appDB.getPhoneBillList();
        }

        if(phoneBillList == null){
            phoneBillList = new ArrayList<PhoneBill>();
        }

        return phoneBillList;

    }

    public void reloadContactsInfo() {

        // Get Distinct Phone Numbers
        ArrayList<String> phoneList = SBAApplicationDB.getInstance().getDistinctPhoneNumbers();

        reloadContactsInfo(phoneList);
    }

    void reloadContactsInfo(ArrayList<String> phoneList){

        ArrayList<String> queries = new ArrayList<String>();
        String query = "";

        Iterator<String> iterator = phoneList.iterator();

        while(iterator.hasNext()){

            String pNo = iterator.next();

            // Get Contact Name and Group Names
            try {

                JSONObject contactData = getContactDetails(pNo);

                if (contactData != null) {

                    String cName = contactData.getString("Name");

                    query = "DELETE FROM ContactNames WHERE PhoneNo = '" + pNo + "'";
                    queries.add(query);

                    query = "DELETE FROM ContactGroups WHERE PhoneNo = '" + pNo + "'";
                    queries.add(query);

                    query = "INSERT INTO ContactNames VALUES(" +
                            "'" + pNo + "'," +
                            "'" + cName + "'" +
                            ")";

                    queries.add(query);

                    JSONArray cGroups = contactData.getJSONArray("Groups");

                    for (int i = 0; i < cGroups.length(); i++) {

                        String gName = cGroups.getString(i);

                        query = "INSERT INTO ContactGroups VALUES(" +
                                "'" + pNo + "'," +
                                "'" + gName + "'" +
                                ")";

                        queries.add(query);

                    }

                }

            } catch (Exception e) {
                // Forget it
                Log.e(SBAApplication.TAG, e.getMessage(), e);
            }
        }

        // Save to DB
        SBAApplicationDB.getInstance().executeQueries(queries);
    }

    // Get Contact Details from Phone Number
    JSONObject getContactDetails(String pNo) throws Exception {

        JSONObject contactData = new JSONObject();

        ContentResolver resolver = getContext().getContentResolver();

        String[] dataProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME};

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(pNo));
        Cursor resultCursor = resolver.query(uri, dataProjection, null, null, null);

        if(resultCursor.moveToFirst()){

            // Get Name
            contactData.put("Name", resultCursor.getString(1));
            long cID = resultCursor.getLong(0);

            resultCursor.close();

            // Get Group IDs
            ArrayList<String> groupIDList = new ArrayList<String>();

            String where = ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.CONTACT_ID + "=?";
            String[] filter = {ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,Long.toString(cID)};

            dataProjection = new String[]{ContactsContract.Data.CONTACT_ID,ContactsContract.Data.DATA1};

            Cursor dataCursor = resolver.query(ContactsContract.Data.CONTENT_URI,dataProjection, where, filter, null );

            if(dataCursor.moveToFirst()){

                groupIDList = new ArrayList<String>();

                do{

                    cID = dataCursor.getLong(0);
                    groupIDList.add(dataCursor.getString(1));

                }while(dataCursor.moveToNext());
            }

            dataCursor.close();

            if(!groupIDList.isEmpty()){

                // Query Group Name
                filter = new String[]{};
                where = ContactsContract.Groups._ID + " IN (?,?,?,?,?,?,?,?,?,?)";
                filter = groupIDList.toArray(filter);

                dataProjection = new String[]{ContactsContract.Groups._ID,ContactsContract.Groups.TITLE};

                Cursor groupCursor = resolver.query(ContactsContract.Groups.CONTENT_URI,dataProjection, where, filter, null);

                if(groupCursor.moveToFirst()){

                    JSONArray groups = new JSONArray();
                    do{
                        groups.put(groupCursor.getString(1));
                    }while(groupCursor.moveToNext());

                    contactData.put("Groups", groups);

                }

                groupCursor.close();

            }
            else{
                // Put Empty Group
                contactData.put("Groups", new JSONArray());
            }

            return contactData;

        }
        else{
            return null;
        }

    }

    public void downloaDBData() {

        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;

        String currentDBPath = "/data/" + "com.ayansh.singtelbillanalyzer"
                + "/databases/" + "SBA";
        String backupDBPath = "SBA";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public JSONArray getMonthlyComparision(){

        SBAApplicationDB appDB = SBAApplicationDB.getInstance();

        String query = "select inb.BillDate, InAmt, OutAmt from " +
                "(select bill.BillNo, bill.BillDate, cd.CallDirection, sum(cd.Amount) as InAmt " +
                "from BillMetaData as bill inner join BillCallDetails as cd on bill.BillNo = cd.BillNo " +
                "where cd.CallDirection = 'In' group by bill.BillDate) as inb " +

                "inner join " +
                "(select bill.BillNo, bill.BillDate, cd.CallDirection, sum(cd.Amount) as OutAmt " +
                "from BillMetaData as bill inner join BillCallDetails as cd on bill.BillNo = cd.BillNo " +
                "where cd.CallDirection = 'Out' group by bill.BillDate) as outb " +

                "on inb.BillNo = outb.BillNo order by inb.BillNo";

        Cursor cursor = appDB.rawQuery(query);

        JSONArray resultData = new JSONArray();

        if(cursor.moveToFirst()){

            do{

                JSONObject data = new JSONObject();

                try {

                    data.put("date", cursor.getString(0));

                    double amt = cursor.getDouble(1);
                    float amount = (float) (Math.round(amt * 100.00) / 100.00);
                    data.put("inamt", amount);

                    amt = cursor.getDouble(2);
                    amount = (float) (Math.round(amt * 100.00) / 100.00);
                    data.put("outamt", amount);

                    resultData.put(data);

                } catch (JSONException e) {
                    // Ignore.
                }

            } while(cursor.moveToNext());

        }

        cursor.close();

        return resultData;

    }

    public void downloadCSVData() {

        String query = "select case when cn.Name is null then cd.PhoneNo else cn.Name end as n, "
                + "cd.* from BillCallDetails as cd "
                + "left outer join ContactNames as cn on cd.PhoneNo = cn.PhoneNo ";

        Cursor cursor = appDB.rawQuery(query);

        File root = Environment.getExternalStorageDirectory();
        File directory = new File(root, "/Android/data/AnalyzeSingTelPhoneBill");
        File file = new File(directory, "BillInfo.csv");

        if (directory.exists()){}
        else{
            directory.mkdirs();
        }

        try {

            FileOutputStream fos = new FileOutputStream(file);

            String line = "BillNo,PhoneNo,ContactName,CallDate,CallTime,CallDuration,Amount," +
                    "CallDirection,Comments,IsFreeCall,IsRoaming,IsSMS";

            fos.write(line.getBytes());

            if(cursor.moveToFirst()){

                do{

                    line = "";

                    line = "\n" +
                            cursor.getString(1) + "," +
                            cursor.getString(2) + "," +
                            cursor.getString(0) + "," +
                            cursor.getString(3) + "," +
                            cursor.getString(4) + "," +
                            cursor.getString(5) + "," +
                            cursor.getFloat(6) + "," +
                            cursor.getString(7) + "," +
                            cursor.getString(8) + "," +
                            cursor.getString(9) + "," +
                            cursor.getString(10) + "," +
                            cursor.getString(11);

                    fos.write(line.getBytes());

                }while(cursor.moveToNext());

            }

            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        cursor.close();
    }
}
