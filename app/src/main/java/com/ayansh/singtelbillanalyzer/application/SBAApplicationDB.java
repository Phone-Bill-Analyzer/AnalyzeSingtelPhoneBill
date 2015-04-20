package com.ayansh.singtelbillanalyzer.application;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by varun on 3/17/15.
 */
public class SBAApplicationDB extends SQLiteOpenHelper {

    private static final String dbName = "SBA";
    private static final int dbVersion = 1;

    private static SBAApplicationDB appDB;

    private SBAApplication app;
    private SQLiteDatabase db;

    private SBAApplicationDB(Context c){

        super(c, dbName, null, dbVersion);
        app = SBAApplication.getInstance();
    }

    static SBAApplicationDB getInstance(Context c){

        if(appDB == null){
            appDB = new SBAApplicationDB(c);
        }

        return appDB;
    }

    static SBAApplicationDB getInstance(){
        return appDB;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createOptionsTable = "CREATE TABLE Options ("
                + "ParamName VARCHAR(20), " + // Parameter Name
                "ParamValue VARCHAR(20)" + // Parameter Value
                ")";

        String createContactNamesTable = "CREATE TABLE ContactNames ("
                + "PhoneNo VARCHAR, " + // Phone No
                "Name VARCHAR" + // Name
                ")";

        String createContactGroupsTable = "CREATE TABLE ContactGroups ("
                + "PhoneNo VARCHAR, " + // Phone No
                "GroupName VARCHAR" + // GroupName
                ")";

        String createBillMetaDataTable = "CREATE TABLE BillMetaData (" +
                "BillNo VARCHAR , " + // Bill No
                "BillType VARCHAR, "  +
                "PhoneNo VARCHAR, "  +
                "BillDate VARCHAR, "  +
                "FromDate VARCHAR, "  +
                "ToDate VARCHAR, "  +
                "DueDate VARCHAR"  +
                ")";

        String createBillCallDetailsTable = "CREATE TABLE BillCallDetails (" +
                "BillNo VARCHAR , " + // Bill No
                "PhoneNo VARCHAR, "  +
                "CallDate VARCHAR, "  +
                "CallTime VARCHAR, "  +
                "CallDuration VARCHAR, "  +
                "Amount VARCHAR, "  +
                "CallDirection VARCHAR, " +
                "Comments VARCHAR, "  +
                "IsFreeCall VARCHAR, " +
                "IsRoaming VARCHAR, " +
                "IsSMS VARCHAR, " +
                "IsSTD VARCHAR, " +
                "Pulse INTEGER" +
                ")";

        // create a new table - if not existing
        try {
            // Create Tables.
            Log.i(SBAApplication.TAG,"Creating Tables for Version:" + String.valueOf(dbVersion));

            db.execSQL(createOptionsTable);
            db.execSQL(createContactNamesTable);
            db.execSQL(createContactGroupsTable);
            db.execSQL(createBillMetaDataTable);
            db.execSQL(createBillCallDetailsTable);

            Log.i(SBAApplication.TAG, "Tables created successfully");

        } catch (SQLException e) {
            Log.e(SBAApplication.TAG, e.getMessage(), e);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    void openDBForWriting(){
        db = appDB.getWritableDatabase();
    }

    synchronized void loadOptions(){

        if(!db.isOpen()){
            return;
        }

        // Load Parameters
        String name, value;
        Log.v(SBAApplication.TAG, "Loading application Options");

        Cursor cursor = db.query("Options", null, null, null, null, null, null);

        if(cursor.moveToFirst()){
            name = cursor.getString(cursor.getColumnIndex("ParamName"));
            value = cursor.getString(cursor.getColumnIndex("ParamValue"));
            app.Options.put(name, value);
        }

        while(cursor.moveToNext()){
            name = cursor.getString(cursor.getColumnIndex("ParamName"));
            value = cursor.getString(cursor.getColumnIndex("ParamValue"));
            app.Options.put(name, value);
        }

        cursor.close();

    }

    synchronized boolean executeQueries(List<String> queries) {

        Iterator<String> iterator = queries.listIterator();
        String query;

        try {
            db.beginTransaction();

            while (iterator.hasNext()) {
                query = iterator.next();
                db.execSQL(query);
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            return true;

        } catch (Exception e) {
            // Do nothing! -- Track the error causing query
            Log.e(SBAApplication.TAG, e.getMessage(), e);
            db.endTransaction();
            return false;
        }
    }

    boolean checkBillNumberExists(String billNo) {

        String[] columns = {"BillNo"};
        String selection = "BillNo='" + billNo + "'";

        Cursor cursor = db.query("BillMetaData", columns, selection, null, null, null, null);

        int count = cursor.getCount();
        cursor.close();

        if(count > 0){
            return true;
        }
        else{
            return false;
        }

    }

    ArrayList<PhoneBill> getPhoneBillList() {

        ArrayList<PhoneBill> billList = new ArrayList<PhoneBill>();

        String sql = "SELECT * FROM BillMetaData";

        Cursor cursor = db.rawQuery(sql, null);

        if(cursor.moveToFirst()){

            do{

                PhoneBill bill = null;

                String billType = cursor.getString(cursor.getColumnIndex("BillType"));

                bill = new PhoneBill(cursor.getString(cursor.getColumnIndex("BillNo")));
                bill.setBillType(billType);

                bill.setPhoneNumber(cursor.getString(cursor.getColumnIndex("PhoneNo")));
                bill.setBillDate(cursor.getString(cursor.getColumnIndex("BillDate")));
                bill.setFromDate(cursor.getString(cursor.getColumnIndex("FromDate")));
                bill.setToDate(cursor.getString(cursor.getColumnIndex("ToDate")));
                bill.setDueDate(cursor.getString(cursor.getColumnIndex("DueDate")));

                billList.add(bill);

            }while(cursor.moveToNext());

        }

        cursor.close();

        Collections.sort(billList, PhoneBill.SortByBillDate);

        return billList;

    }

    ArrayList<String> getDistinctPhoneNumbers(){

        String query = "SELECT DISTINCT PhoneNo from BillCallDetails";

        Cursor cursor = rawQuery(query);

        ArrayList<String> phoneList = new ArrayList<String>();

        if(cursor.moveToFirst()){

            do{

                phoneList.add(cursor.getString(0));

            }while(cursor.moveToNext());

        }

        cursor.close();

        return phoneList;

    }

    ArrayList<String> getDistinctPhoneNumbers(String billNo){

        String query = "SELECT DISTINCT PhoneNo from BillCallDetails where BillNo = '" + billNo + "'";

        Cursor cursor = rawQuery(query);

        ArrayList<String> phoneList = new ArrayList<String>();

        if(cursor.moveToFirst()){

            do{

                phoneList.add(cursor.getString(0));

            }while(cursor.moveToNext());

        }

        cursor.close();

        return phoneList;
    }

    Cursor rawQuery(String query){

        return db.rawQuery(query, null);

    }
}
