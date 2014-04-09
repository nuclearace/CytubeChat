package com.milkbartube.tracy;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.sound.sampled.*;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatFrame extends javax.swing.JFrame implements ChatCallbackAdapter, WindowFocusListener {

    private static final long serialVersionUID = -3120953406569989166L;

    private Chat chat;

    public ChatFrame() {
	initComponents();
	setVisible(true);
	setLocationRelativeTo(null);
	disableNewMessages();
	try {
	    URL soundFile = new URL("http://cytu.be/boop.wav");
	    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);

	    this.clip = AudioSystem.getClip();
	    clip.open(audioIn);
	} catch (Exception e) {
	    this.clip = null;
	    e.printStackTrace();
	}

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

	NewMessageTextField.setBorder(null);
	NewMessageTextField.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		NewMessageActionPerformed(evt);
	    }
	});

	addWindowFocusListener(this);
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
    private ArrayList<CytubeUser> userList = new ArrayList<CytubeUser>();
    private Clip clip;
    private boolean muteBoop = false;


    public void disableNewMessages() {
	NewMessageTextField.setEnabled(false);
    }

    public void enableNewMessages() {
	NewMessageTextField.setEnabled(true);
    }

    public void handleGUICommand(String data) {
	data = data.replace("\n", "").replace("\r", "");
	if (!data.equals("")) {
	    String[] parts = data.split(" ");
	    String command = parts[0];
	    if (command.equals("/disconnect")) {
		chat.disconnectChat();
		userListTextArea.setText("");
		setTitle("Disconnected!");
	    } else if (command.equals("/reconnect")) {
		chat.disconnectChat();
		MessagesTextArea.setText("Connecting...");
		chat.reconnectChat();
	    } else if (command.equals("/grey")) {
		// Begin color prefs
		MessagesTextArea.setBackground(new java.awt.Color(71, 77, 70));
		MessagesTextArea.setForeground(new java.awt.Color(255, 255, 255));

		userListTextArea.setBackground(new java.awt.Color(71, 77, 70));
		userListTextArea.setForeground(new java.awt.Color(255, 255, 255));

		NewMessageTextField.setBackground(new java.awt.Color(71, 77, 70));
		NewMessageTextField.setForeground(new java.awt.Color(255, 255, 255));
	    } else if (command.equals("/black")) {
		MessagesTextArea.setBackground(new java.awt.Color(0, 0, 0));
		MessagesTextArea.setForeground(new java.awt.Color(255, 255, 255));

		userListTextArea.setBackground(new java.awt.Color(0, 0, 0));
		userListTextArea.setForeground(new java.awt.Color(255, 255, 255));

		NewMessageTextField.setBackground(new java.awt.Color(0, 0, 0));
		NewMessageTextField.setForeground(new java.awt.Color(255, 255, 255));
	    } else if (command.equals("/white")) {
		MessagesTextArea.setBackground(new java.awt.Color(255, 255, 255));
		MessagesTextArea.setForeground(new java.awt.Color(0, 0, 0));

		userListTextArea.setBackground(new java.awt.Color(255, 255, 255));
		userListTextArea.setForeground(new java.awt.Color(0, 0 , 0));

		NewMessageTextField.setBackground(new java.awt.Color(255, 255, 255));
		NewMessageTextField.setForeground(new java.awt.Color(0, 0, 0));
		// End color prefs
	    } else if (command.equals("/clearchat")) {
		MessagesTextArea.setText("");
	    } else if (command.equals("/pm")) {
		// This could be done better, but I don't want to take the time
		if (parts.length > 2) {
		    String to = parts[1];
		    String message = "";
		    String[] messageArray = Arrays.copyOfRange(parts, 2, parts.length);

		    for (String word: messageArray) {
			message += word + " ";
		    }
		    try {
			this.privateMessage(to, message);
		    } catch (JSONException e) {
			e.printStackTrace();
		    }
		} else
		    return;
	    } else 
		chat.sendMessage(data);

	} else
	    return;
    }

    @Override
    public void callback(JSONArray data) throws JSONException {}

    @Override
    public void on(String event, JSONObject obj) {
	try {
	    if (event.equals("chatMsg")) {
		this.chatMsg(obj);
	    } else if (event.equals("addUser")) {
		boolean afk = (boolean) obj.getJSONObject("meta").get("afk");
		String username = obj.getString("name") ;
		int rank = (int) obj.get("rank");
		CytubeUser user = new CytubeUser(afk, username, rank);
		this.addUser(user);
		this.updateUserList();
	    } else if (event.equals("userLeave")) {
		this.removeUser(obj.getString("name"));
		this.updateUserList();
	    } else if (event.equals("changeMedia")) {
		this.changeMedia(obj.getString("title"));
	    } else if (event.equals("pm")) {
		this.onPrivateMessage(obj);
	    } else if (event.equals("setAfk")) {
		this.setAfk(obj.getString("name"), (boolean) obj.get("afk"));
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
	} else {
	    JOptionPane.showMessageDialog(null, 
		    "Error: room and username needed");
	    this.onConnect();
	}
    }

    @Override
    public void onDisconnect() {
	MessagesTextArea.setText("Disconnected!\n");
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
		boolean afk = (boolean) users.getJSONObject(i).getJSONObject("meta")
			.get("afk");
		String username = (String) users.getJSONObject(i).get("name");
		int rank = (int) users.getJSONObject(i).get("rank");
		CytubeUser user = new CytubeUser(afk, username, rank);
		this.addUser(user);
	    }
	    this.updateUserList();
	}
    }

    public void addUser(CytubeUser user) {
	if (!userList.contains(user)) {
	    userList.add(user);
	}
    }

    public void changeMedia(String media) {
	setTitle("Now Playing: " + media);
    }

    public void chatMsg(JSONObject obj) throws JSONException {
	String cleanedString = StringEscapeUtils.unescapeHtml4(obj.getString("msg"));
	cleanedString = cleanedString.replaceAll("\\<.*?\\>", "");
	if (!cleanedString.equals("")) {
	    long time = (long) obj.get("time");
	    Date date = new Date(time);
	    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss z");
	    formatter.setTimeZone(TimeZone.getDefault());
	    String formattedTime = formatter.format(date);

	    MessagesTextArea.append("[" + formattedTime + "] " +
		    obj.getString("username") + ": " + cleanedString + "\n");
	    MessagesTextArea.setCaretPosition(MessagesTextArea.getDocument().getLength());

	    if (this.clip != null && this.isMuteBoop()) {
		this.playSound();
	    }
	}
    }

    public void onPrivateMessage(JSONObject obj) throws JSONException {
	String cleanedString = StringEscapeUtils.unescapeHtml4(obj.getString("msg"));
	cleanedString = cleanedString.replaceAll("\\<.*?\\>", "");
	if (!cleanedString.equals("")) {
	    long time = (long) obj.get("time");
	    Date date = new Date(time);
	    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss z");
	    formatter.setTimeZone(TimeZone.getDefault());
	    String formattedTime = formatter.format(date);

	    MessagesTextArea.append("[" + formattedTime + "] " + 
		    obj.getString("username") + " [Private Message]: " 
		    + cleanedString + "\n");
	    MessagesTextArea.setCaretPosition(MessagesTextArea.getDocument().getLength());

	    if (this.clip != null && this.isMuteBoop()) {
		this.playSound();
	    }
	}
    }

    private void playSound() {
	this.clip.start();
	this.clip.setFramePosition(0);
    }

    private void privateMessage(String to, String message) throws JSONException {
	JSONObject json = new JSONObject();
	json.putOpt("to", to);
	json.putOpt("msg", message);
	json.putOpt("meta", "");

	chat.privateMessage(json);
    }

    public void removeUser(String username) {
	for (CytubeUser user : userList) {
	    if (user.getName().equals(username)) {
		userList.remove(user);
		break;
	    }
	}
    }

    public boolean isMuteBoop() {
	return muteBoop;
    }

    public void setMuteBoop(boolean muteBoop) {
	this.muteBoop = muteBoop;
    }

    private void setAfk(String name, boolean afk) {
	for (CytubeUser user : userList) {
	    if (user.getName().equals(name)) {
		user.setAfk(afk);
		break;
	    }
	}
    }

    public void updateUserList() {
	// Number of users. Note: I'm ignoring anons at this time
	String str = "Users: " + userList.size() + "\n-----------------\n";

	//Sort userlist
	Collections.sort(userList, new Comparator<CytubeUser>() {
	    @Override
	    public int compare(CytubeUser user1, CytubeUser user2) {
		return user1.getName().compareToIgnoreCase(user2.getName());
	    }
	});
	for (CytubeUser user : userList) {
	    switch (user.getRank()) {
	    case 2:
		str += "~" + user.getName() + "\n";
		break;
	    case 3:
		str += "@" + user.getName() + "\n";
		break;
	    case 4:
		str += "@" + user.getName() + "\n";
		break;
	    case 5:
		str += "$" + user.getName() + "\n";
		break;
	    case 255:
		str += "%" + user.getName() + "\n";
		break;
	    default:
		str += user.getName() + "\n";
		break;
	    }
	}
	userListTextArea.setText(str);
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
	this.muteBoop = false;
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
	this.muteBoop = true;
    }
}