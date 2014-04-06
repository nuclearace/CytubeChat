package com.milkbartube.tracy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatFrame extends javax.swing.JFrame implements ChatCallbackAdapter {

    private static final long serialVersionUID = -3120953406569989166L;

    private Chat chat;


    public ChatFrame() {
	initComponents();
	setVisible(true);
	setLocationRelativeTo(null);
	disableNewMessages();

	startChat();
    }

    private void initComponents() {

	MessagesScrollPane = new javax.swing.JScrollPane();
	MessagesTextArea = new javax.swing.JTextArea();
	userListScrollPane = new javax.swing.JScrollPane();
	userListTextArea = new javax.swing.JTextArea();
	NewMessageScrollPane = new javax.swing.JScrollPane();
	NewMessageTextField = new javax.swing.JTextField();

	setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

	MessagesTextArea.setEditable(false);
	MessagesTextArea.setColumns(20);
	MessagesTextArea.setLineWrap(true);
	MessagesTextArea.setRows(5);
	MessagesTextArea.setFocusable(false);
	MessagesScrollPane.setViewportView(MessagesTextArea);

	userListScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	userListScrollPane.setFocusTraversalKeysEnabled(false);
	userListScrollPane.setFocusable(false);

	userListTextArea.setEditable(false);
	userListTextArea.setColumns(20);
	userListTextArea.setRows(5);
	userListTextArea.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
	userListTextArea.setFocusTraversalKeysEnabled(false);
	userListTextArea.setFocusable(false);
	userListScrollPane.setViewportView(userListTextArea);

	NewMessageTextField.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		NewMessageActionPerformed(evt);
	    }
	});
	NewMessageScrollPane.setViewportView(NewMessageTextField);

	javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	getContentPane().setLayout(layout);
	layout.setHorizontalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(NewMessageScrollPane)
				.addGroup(layout.createSequentialGroup()
					.addComponent(userListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(MessagesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)))
					.addContainerGap())
		);
	layout.setVerticalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(MessagesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
				.addComponent(userListScrollPane))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(NewMessageScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap())
		);

	pack();
    }

    private void NewMessageActionPerformed(java.awt.event.ActionEvent evt) {                                           
	this.handleGUICommand(NewMessageTextField.getText());
	NewMessageTextField.setText(null);
    }    

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	java.awt.EventQueue.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		new ChatFrame();
	    }
	});
    }


    public void startChat() {
	MessagesTextArea.append("Connecting...");
	chat = new Chat(this);
	chat.start();
    }

    private javax.swing.JScrollPane MessagesScrollPane;
    private javax.swing.JTextArea MessagesTextArea;
    private javax.swing.JScrollPane NewMessageScrollPane;
    private javax.swing.JTextField NewMessageTextField;
    private javax.swing.JScrollPane userListScrollPane;
    private javax.swing.JTextArea userListTextArea;
    private ArrayList<String> userList = new ArrayList<String>();


    public void disableNewMessages() {
	NewMessageTextField.setEnabled(false);
    }

    public void enableNewMessages() {
	NewMessageTextField.setEnabled(true);
    }

    public void handleGUICommand(String data) {
	data = data.replace("\n", "").replace("\r", "");
	if (!data.equals("")) {
	    if (data.equals("/disconnect")) {
		chat.disconnectChat();
		userListTextArea.setText("");
	    }
	    else if (data.equals("/reconnect")) {
		MessagesTextArea.setText("Connecting...");
		chat.reconnectChat();
	    }
	    else {
		chat.sendMessage(data);
	    }
	}
	else
	    return;
    }

    @Override
    public void callback(JSONArray data) throws JSONException {}

    @Override
    public void on(String event, JSONObject obj) {
	try {
	    if (event.equals("chatMsg")) {
		this.chatMsg(obj);
	    }
	    else if (event.equals("addUser")) {
		this.addUser(obj.getString("name"));
	    }
	    else if (event.equals("userLeave")) {
		this.removeUser(obj.getString("name"));
	    }
	    else if (event.equals("changeMedia")) {
		this.changeMedia(obj.getString("title"));
	    }
	} catch (JSONException ex) {
	    ex.printStackTrace();
	}
    }

    @Override
    public void onMessage(String message) {}

    @Override
    public void onMessage(JSONObject json) {}

    @Override
    public void onConnect() {
	MessagesTextArea.append("done!\n");        
	enableNewMessages();
	String room = JOptionPane.showInputDialog(null, "Room", null, WIDTH);
	String nickname = JOptionPane.showInputDialog(null, "Nickname", null, WIDTH);
	String password = JOptionPane.showInputDialog(null, "Password", null, WIDTH);
	if (!nickname.isEmpty() && !room.isEmpty()) {
	    chat.join(room, nickname, password);
	    MessagesTextArea.append("You joined as " + nickname + "\n");
	}
	else {
	    JOptionPane.showMessageDialog(null, 
		    "Error: room and username needed");
	    this.onConnect();
	}
    }

    @Override
    public void onDisconnect() {
	MessagesTextArea.setText("Disconnected!");
    }

    @Override
    public void onConnectFailure() {
	MessagesTextArea.append("error!\n");
    }

    @Override
    public void onArray(String event, JSONArray data) throws JSONException {
	if (event.equals("userlist")) {
	    userList.clear();
	    JSONArray users = data.getJSONArray(0);
	    for (int i=0; i<users.length();i++) {
		String user = (String) users.getJSONObject(i).get("name");
		userList.add(user);
	    }
	    this.updateUserList();
	}

    }

    public void addUser(String user) {
	if (!userList.contains(user)) {
	    userList.add(user);
	    this.updateUserList();
	}
    }

    public void changeMedia(String media) {
	setTitle("Now Playing: " + media);
    }

    public void chatMsg(JSONObject obj) throws JSONException {
	String cleanedString = StringEscapeUtils.unescapeHtml4(obj.getString("msg"));
	cleanedString = cleanedString.replaceAll("\\<.*?\\>", "");
	if (!cleanedString.equals("")) {
	    MessagesTextArea.append(obj.getString("username") + ": " + cleanedString + "\n");
	    MessagesTextArea.setCaretPosition(MessagesTextArea.getDocument().getLength());
	}
    }

    public void removeUser(String user) {
	if (userList.contains(user)) {
	    userList.remove(user);
	    this.updateUserList();
	}
    }

    public void updateUserList() {
	String str = "";

	//Sort userlist
	Collections.sort(userList, new Comparator<String>() {
	    @Override
	    public int compare(String user1, String user2) {
		return user1.compareToIgnoreCase(user2);
	    }

	});

	for (String s : userList) {
	    str += s + "\n";
	}
	userListTextArea.setText(str);
    }
}