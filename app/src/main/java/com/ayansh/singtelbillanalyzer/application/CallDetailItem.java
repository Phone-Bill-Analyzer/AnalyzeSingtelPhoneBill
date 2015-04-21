package com.ayansh.singtelbillanalyzer.application;

import org.json.JSONException;
import org.json.JSONObject;

public class CallDetailItem {

	private String callDate, callTime, phoneNumber, duration, comments;
	private float cost;
	private String freeCall, roamingCall, smsCall, stdCall, callDirection;
	private int pulse;
	
	public CallDetailItem(){
		callDate = callTime = phoneNumber = duration = comments = "";
		cost = 0;
		freeCall = roamingCall = smsCall = stdCall = callDirection = "";
		pulse = 0;
	}
	
	public CallDetailItem(JSONObject cd) {

		callDate = callTime = phoneNumber = duration = comments = "";
		cost = 0;
		freeCall = roamingCall = smsCall = stdCall = callDirection = "";
		pulse = 0;
		
		try {
			
			callDate = cd.getString("callDate");
			callTime = cd.getString("callTime");
			phoneNumber = cd.getString("phoneNumber");
			duration = cd.getString("duration");
			cost = (float) cd.getDouble("cost");
			comments = cd.getString("comments");
            callDirection = cd.getString("callDirection");

			setFreeCall(cd.getString("freeCall"));
			setRoamingCall(cd.getString("roamingCall"));
			setSmsCall(cd.getString("smsCall"));
			setStdCall(cd.getString("stdCall"));
			setPulse(cd.getInt("pulse"));
			
		} catch (JSONException e) {
			// Can't do anything :(
		}
		
	}

	public String getCallDate() {
		return callDate;
	}
	public void setCallDate(String callDate) {
		this.callDate = callDate;
	}
	
	public String getCallTime() {
		return callTime;
	}
	public void setCallTime(String callTime) {
		this.callTime = callTime;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public float getCost() {
		return cost;
	}
	public void setCost(float cost) {
		this.cost = cost;
	}
	
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getFreeCall() {
		return freeCall;
	}
	public void setFreeCall(String freeCall) {
		this.freeCall = freeCall;
	}

	public String getRoamingCall() {
		return roamingCall;
	}
	public void setRoamingCall(String roamingCall) {
		this.roamingCall = roamingCall;
	}

	public String getSmsCall() {
		return smsCall;
	}
	public void setSmsCall(String smsCall) {
		this.smsCall = smsCall;
	}

	public String getStdCall() {
		return stdCall;
	}
	public void setStdCall(String stdCall) {
		this.stdCall = stdCall;
	}

	public int getPulse() {
		return pulse;
	}
	public void setPulse(int pulse) {
		this.pulse = pulse;
	}

    public String getCallDirection() {
        return callDirection;
    }
    public void setCallDirection(String callDirection) {
        this.callDirection = callDirection;
    }
}