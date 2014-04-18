package com.milkbartube.tracy;

import java.net.URISyntaxException;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class AlternativeChat {

    private Socket socket;

    public  AlternativeChat() {
	try {
	    socket = IO.socket("http://milkbartube.com:8088");
	} catch (URISyntaxException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

	    @Override
	    public void call(Object... args) {
		System.out.println("connected");
		//socket.emit("initChannelCallbacks");
		//socket.disconnect();
	    }

	});

	socket.on(Socket.EVENT_ERROR,  new Emitter.Listener() {

	    @Override
	    public void call(Object... args) {
		System.out.println(args.toString());

	    }

	});

	socket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {

	    @Override
	    public void call(Object... args) {
		System.out.println("something happened");

	    }

	});
	socket.connect();
    }

    public Socket getSocket() {
	return socket;
    }

    public void setSocket(Socket socket) {
	this.socket = socket;
    }
}
