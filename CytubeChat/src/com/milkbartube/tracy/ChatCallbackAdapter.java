package com.milkbartube.tracy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface ChatCallbackAdapter {
    public void callback(JSONArray data) throws JSONException;
    public void on(String event, JSONObject data);
    public void onArray(String event, JSONArray data) throws JSONException;
    public void onBoolean(String event, boolean bool);
    public void onMessage(String message);
    public void onMessage(JSONObject json);
    public void onConnect();
    public void onDisconnect();
    public void onConnectFailure();
}
