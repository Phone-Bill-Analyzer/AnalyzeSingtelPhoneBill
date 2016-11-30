package com.ayansh.singtelbillanalyzer.application;

import android.database.Cursor;
import android.net.Uri;

import com.ayansh.CommandExecuter.Command;
import com.ayansh.CommandExecuter.Invoker;
import com.ayansh.CommandExecuter.ResultObject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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

		String postURL = "http://apps.ayansh.com/Phone-Bill-Analyzer/sync_from_mobile.php";

		URL url = new URL(postURL);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

		urlConnection.setDoOutput(true);
		urlConnection.setChunkedStreamingMode(0);
		urlConnection.setRequestMethod("POST");

		JSONObject input = getDataForSync();

		Uri.Builder uriBuilder = new Uri.Builder()
				.appendQueryParameter("session_id", sid)
				.appendQueryParameter("data", input.toString());

		String parameterQuery = uriBuilder.build().getEncodedQuery();

		OutputStream os = urlConnection.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.write(parameterQuery);
		writer.flush();
		writer.close();
		os.close();

		urlConnection.connect();

		InputStream in = new BufferedInputStream(urlConnection.getInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject output = new JSONObject(builder.toString());

		int error_code = output.getInt("ErrorCode");
		if(error_code == 0){

		}
		else{
			throw new Exception(output.getString("Message"));
		}

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
				object.put("PhoneNumber", cursor.getString(cursor.getColumnIndex("PhoneNo")));
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
				object.put("phoneNumber", cursor.getString(cursor.getColumnIndex("PhoneNo")));
				object.put("callDate", cursor.getString(cursor.getColumnIndex("CallDate")));
				object.put("callTime", cursor.getString(cursor.getColumnIndex("CallTime")));
				object.put("duration", cursor.getString(cursor.getColumnIndex("CallDuration")));
				object.put("cost", cursor.getFloat(cursor.getColumnIndex("Amount")));
				object.put("callDirection", cursor.getString(cursor.getColumnIndex("CallDirection")));
				object.put("comments", cursor.getString(cursor.getColumnIndex("Comments")));
				object.put("freeCall", cursor.getString(cursor.getColumnIndex("IsFreeCall")));
				object.put("roamingCall", cursor.getString(cursor.getColumnIndex("IsRoaming")));
				object.put("smsCall", cursor.getString(cursor.getColumnIndex("IsSMS")));
				object.put("stdCall", cursor.getString(cursor.getColumnIndex("IsSTD")));
				object.put("pulse", cursor.getInt(cursor.getColumnIndex("Pulse")));

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
