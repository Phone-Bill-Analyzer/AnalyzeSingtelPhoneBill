package com.ayansh.singtelbillanalyzer.application;

import android.database.Cursor;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ResultObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WebSyncCommand extends Command {

	private String sid;


	public WebSyncCommand(Invoker caller, String sid) {
		
		super(caller);
		this.sid = sid;
	}

	@Override
	protected void execute(ResultObject result) throws Exception {

		String url = "http://apps.ayansh.com/Phone-Bill-Analyzer/sync_from_mobile.php";

		JSONObject input = getDataForSync();

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("session_id", sid));
		nameValuePairs.add(new BasicNameValuePair("data", input.toString()));
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		// Execute HTTP Post Request
		HttpResponse response = httpclient.execute(httppost);

		// Open Stream for Reading.
		InputStream is = response.getEntity().getContent();

		// Get Input Stream Reader.
		InputStreamReader isr = new InputStreamReader(is);

		BufferedReader reader = new BufferedReader(isr);

		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject output = new JSONObject(builder.toString());

	}


	private JSONObject getDataForSync() throws Exception{

		JSONObject data = new JSONObject();

		// Bill Meta Data
		String sql = "SELECT * FROM BillMetaData";
		Cursor cursor = SBAApplicationDB.getInstance().rawQuery(sql);

		JSONArray billMetaData = new JSONArray();
		JSONObject object = new JSONObject();

		if(cursor.moveToFirst()){

			do{

				object = new JSONObject();

				object.put("BillNo",cursor.getString(cursor.getColumnIndex("BillNo")));
				object.put("BillType", cursor.getString(cursor.getColumnIndex("BillType")));
				object.put("PhoneNo", cursor.getString(cursor.getColumnIndex("PhoneNo")));
				object.put("BillDate", cursor.getString(cursor.getColumnIndex("BillDate")));
				object.put("FromDate", cursor.getString(cursor.getColumnIndex("FromDate")));
				object.put("ToDate", cursor.getString(cursor.getColumnIndex("ToDate")));
				object.put("DueDate", cursor.getString(cursor.getColumnIndex("DueDate")));

				billMetaData.put(object);

			}while(cursor.moveToNext());

		}

		cursor.close();
		data.put("BillMetaData", billMetaData);

		// Bill Call Details
		sql = "SELECT * FROM BillCallDetails";
		cursor = SBAApplicationDB.getInstance().rawQuery(sql);

		JSONArray callDetails = new JSONArray();

		if(cursor.moveToFirst()){

			do{

				object = new JSONObject();

				object.put("BillNo",cursor.getString(cursor.getColumnIndex("BillNo")));
				object.put("PhoneNo", cursor.getString(cursor.getColumnIndex("PhoneNo")));
				object.put("CallDate", cursor.getString(cursor.getColumnIndex("CallDate")));
				object.put("CallTime", cursor.getString(cursor.getColumnIndex("CallTime")));
				object.put("CallDuration", cursor.getString(cursor.getColumnIndex("CallDuration")));
				object.put("Amount", cursor.getFloat(cursor.getColumnIndex("Amount")));
				object.put("CallDirection", cursor.getString(cursor.getColumnIndex("CallDirection")));
				object.put("Comments", cursor.getString(cursor.getColumnIndex("Comments")));
				object.put("IsFreeCall", cursor.getString(cursor.getColumnIndex("IsFreeCall")));
				object.put("IsRoaming", cursor.getString(cursor.getColumnIndex("IsRoaming")));
				object.put("IsSMS", cursor.getString(cursor.getColumnIndex("IsSMS")));
				object.put("IsSTD", cursor.getString(cursor.getColumnIndex("IsSTD")));
				object.put("Pulse", cursor.getInt(cursor.getColumnIndex("Pulse")));

				callDetails.put(object);

			}while(cursor.moveToNext());

		}

		cursor.close();
		data.put("CallDetails", callDetails);

		// Contact Names
		sql = "SELECT * FROM ContactNames";
		cursor = SBAApplicationDB.getInstance().rawQuery(sql);

		JSONArray contactNames = new JSONArray();

		if(cursor.moveToFirst()){

			do{

				object = new JSONObject();

				object.put("PhoneNo", cursor.getString(cursor.getColumnIndex("PhoneNo")));
				object.put("Name", cursor.getString(cursor.getColumnIndex("Name")));

				contactNames.put(object);

			}while(cursor.moveToNext());

		}

		cursor.close();
		data.put("ContactNames", contactNames);

		// Contact Group
		sql = "SELECT * FROM ContactGroups";
		cursor = SBAApplicationDB.getInstance().rawQuery(sql);

		JSONArray contactGroups = new JSONArray();

		if(cursor.moveToFirst()){

			do{

				object = new JSONObject();

				object.put("PhoneNo", cursor.getString(cursor.getColumnIndex("PhoneNo")));
				object.put("GroupName", cursor.getString(cursor.getColumnIndex("GroupName")));

				contactGroups.put(object);

			}while(cursor.moveToNext());

		}

		cursor.close();
		data.put("ContactGroups", contactGroups);

		return data;

	}
}
