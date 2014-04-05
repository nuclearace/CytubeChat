package com.milkbartube.tracy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

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
	NewMessageScrollPane = new javax.swing.JScrollPane();
	NewMessageTextArea = new javax.swing.JTextField();;
	OnlineUsersLabel = new javax.swing.JLabel();
	OnlineUsers = new javax.swing.JLabel();

	setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	setTitle("Chat");


	MessagesTextArea.setColumns(20);
	MessagesTextArea.setEditable(false);
	MessagesTextArea.setRows(5);
	MessagesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	MessagesScrollPane.setViewportView(MessagesTextArea);

	NewMessageTextArea.setColumns(20);
	//NewMessageTextArea.setRows(5);
	NewMessageScrollPane.setViewportView(NewMessageTextArea);

	NewMessageTextArea.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent event){
		NewMessageActionPerformed(event);
	    }
	}
		);

	OnlineUsersLabel.setText("Online:");

	javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	getContentPane().setLayout(layout);
	layout.setHorizontalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
			.addContainerGap()
			.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(MessagesScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
					.addComponent(NewMessageScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 295, Short.MAX_VALUE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
					.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
						.addComponent(OnlineUsersLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(OnlineUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)))
						.addContainerGap())
		);
	layout.setVerticalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
			.addContainerGap()
			.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(OnlineUsersLabel)
				.addComponent(OnlineUsers))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(MessagesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
					.addComponent(NewMessageScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE))
					.addContainerGap())
		);

	pack();
    }

    private void NewMessageActionPerformed(java.awt.event.ActionEvent evt) {
	this.handleGUICommand(NewMessageTextArea.getText());
	NewMessageTextArea.setText(null);
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
    private JTextField NewMessageTextArea;
    private javax.swing.JLabel OnlineUsers;
    private javax.swing.JLabel OnlineUsersLabel;


    public void disableNewMessages() {
	NewMessageTextArea.setEnabled(false);
    }

    public void enableNewMessages() {
	NewMessageTextArea.setEnabled(true);
    }

    public void handleGUICommand(String data) {
	if (data.equals("/disconnect")) {
	    chat.disconnectChat();
	}

	else if (data.equals("/reconnect")) {
	    MessagesTextArea.setText("Connecting...");
	    chat.reconnectChat();
	}

	else {
	    chat.sendMessage(data);
	}
    }

    @Override
    public void callback(JSONArray data) throws JSONException {}

    @Override
    public void on(String event, JSONObject obj) {
	try {
	    if (event.equals("chatMsg")) {
		String cleanedString = StringEscapeUtils.unescapeHtml4(obj.getString("msg"));
		MessagesTextArea.append(obj.getString("username") + ": " + cleanedString + "\n");
		MessagesTextArea.setCaretPosition(MessagesTextArea.getDocument().getLength());
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
	    this.addUser(data.getJSONArray(0));
	}

    }
    public void addUser(JSONArray users) throws JSONException {
	String str = "";
	for (int i=0; i<users.length();i++) {
	    if (i != 0) {
		str += ", ";
	    }
	    str += users.getJSONObject(i).get("name");
	}
	OnlineUsers.setText(str);
    }
}