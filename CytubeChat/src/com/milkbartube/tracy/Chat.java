package com.milkbartube.tracy;

import io.socket.SocketIO;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

public class Chat extends Thread {
    private SocketIO socket;
    private ChatCallback callback;

    public Chat(ChatCallbackAdapter callback) {
	this.callback = new ChatCallback(callback);
    }

    @Override
    public void run() {
	try {
	    socket = new SocketIO("http://sea.cytu.be:8880/", callback);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	}
    }
    
    public void disconnectChat() {
	socket.disconnect();
    }
    
    public void reconnectChat() {
	try {
	    socket = new SocketIO("http://sea.cytu.be:8880/", callback);
	} catch (MalformedURLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public void sendMessage(String message) {
	try {
	    JSONObject json = new JSONObject();
	    json.putOpt("msg", message);
	    socket.emit("chatMsg", json);
	} catch (JSONException ex) {
	    ex.printStackTrace();
	}
    }

    public void join(String room, String nickname, String password) {
	try {
	    JSONObject json = new JSONObject();
	    json.putOpt("name", room);
	    System.out.println(json);
	    socket.emit("initChannelCallbacks");
	    socket.emit("joinChannel", json);

	    JSONObject json1 = new JSONObject();
	    json1.putOpt("name", nickname);
	    json1.putOpt("pw", password);
	    socket.emit("login", callback, json1);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
