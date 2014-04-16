package com.milkbartube.tracy;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;

import javax.swing.JTextArea;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class ChatPanel extends JPanel implements ChatCallbackAdapter {

    private static final long serialVersionUID = 1L;
    private JScrollPane messagesScrollPane;
    private JTextArea messagesTextArea;
    private JScrollPane newMessageScrollPane;
    private JTextField newMessageTextField;
    private JScrollPane userListScrollPane;
    private JTextArea userlistTextArea;

    private Chat chat;
    private String currentMedia = "";
    private ChatFrame parent;
    private String room = "";
    private String roomPassword = "";
    private String username = "";
    private LinkedList<String> messageBuffer = new LinkedList<String>();
    private ArrayList<CytubeUser> userList = new ArrayList<CytubeUser>();
    private CytubeUser user = new CytubeUser(false, "", 0);

    public ChatPanel(String username, String room, String password, ChatFrame frame) {
	buildChatPanel();
	this.room = room;
	this.roomPassword = password;
	this.username = username;
	this.parent = frame;

	chat = new Chat(this);
	chat.start();
    }

    /**
     * Create the panel.
     */
    public void buildChatPanel() {

	userListScrollPane = new JScrollPane();

	messagesScrollPane = new JScrollPane();

	newMessageScrollPane = new JScrollPane();

	GroupLayout groupLayout = new GroupLayout(this);
	groupLayout.setHorizontalGroup(
		groupLayout.createParallelGroup(Alignment.LEADING)
		.addGroup(groupLayout.createSequentialGroup()
			.addComponent(userListScrollPane, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(messagesScrollPane, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE))
			.addComponent(newMessageScrollPane, GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
		);
	groupLayout.setVerticalGroup(
		groupLayout.createParallelGroup(Alignment.LEADING)
		.addGroup(groupLayout.createSequentialGroup()
			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(messagesScrollPane)
				.addComponent(userListScrollPane, GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(newMessageScrollPane, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
				.addContainerGap())
		);

	setNewMessageTextField(new JTextField());
	getNewMessageTextField().addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		NewMessageActionPerformed(e);
	    }
	});
	getNewMessageTextField().setBorder(null);
	newMessageScrollPane.setViewportView(getNewMessageTextField());
	getNewMessageTextField().setColumns(10);
	getNewMessageTextField().addKeyListener(new KeyListener() {
	    @Override
	    public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == '\t') {
		    handleTabComplete();
		}
	    }

	    @Override
	    public void keyPressed(KeyEvent e) {}

	    @Override
	    public void keyReleased(KeyEvent e) {}
	});

	setMessagesTextArea(new JTextArea());
	getMessagesTextArea().setEditable(false);
	messagesScrollPane.setViewportView(getMessagesTextArea());

	setUserlistTextArea(new JTextArea());
	getUserlistTextArea().setEditable(false);
	userListScrollPane.setViewportView(getUserlistTextArea());
	setLayout(groupLayout);

    }

    private void addUser(CytubeUser user, boolean fromAddUser) {
	if (user.getName().toLowerCase().equals(
		this.user.getName().toLowerCase())) {
	    this.user = user;
	}
	if (this.user.getRank() <= 1  && fromAddUser) {
	    getMessagesTextArea().append(formatMessage("[Client]", 
		    user.getName() + " joined the room", 
		    System.currentTimeMillis(), false));
	}
	if (!userList.contains(user)) {
	    userList.add(user);
	}

    }

    private void chatMsg(JSONObject obj) throws JSONException {
	String message = 
		this.formatMessage(obj.getString("username"), 
			obj.getString("msg"), (long) obj.get("time"), false);

	if (messageBuffer.size() > 100 && parent.isLimitChatBuffer()) {
	    messageBuffer.remove();
	    messagesTextArea.setText(getMessagesTextArea().getText()
		    .substring(getMessagesTextArea().getText().indexOf('\n')+1));
	}

	messageBuffer.add(message);
	messagesTextArea.append(message);
	getMessagesTextArea().setCaretPosition(getMessagesTextArea().getDocument().getLength());

	if (parent.getClip() != null && parent.isWindowFocus() && !parent.isUserMuteBoop()
		|| obj.getString("msg").toLowerCase().contains(getUsername())) {
	    parent.playSound();
	}

    }

    public String formatMessage(String username, String message, long time, boolean privateMessage) {
	String cleanedString = StringEscapeUtils.unescapeHtml4(message);
	String imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
	cleanedString = cleanedString.replaceAll(imgRegex, "$1");
	cleanedString = cleanedString.replaceAll("\\<.*?\\>", "");

	// Add the timestamp
	Date date = new Date(time);
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
	formatter.setTimeZone(TimeZone.getDefault());
	String formattedTime = formatter.format(date);
	message = "[" + formattedTime + "] ";

	if (privateMessage) 
	    message += "[Private Message] ";

	return message += username + ": " + cleanedString + "\n";
    }

    public void handleGUICommand(String data) {
	data = data.replace("\n", "").replace("\r", "");
	if (!data.equals("")) {
	    String[] parts = data.split(" ");
	    String command = parts[0];
	    if (command.equals("/disconnect")) {
		chat.disconnectChat();
		userlistTextArea.setText("");
		messagesTextArea.setText("Disconnected");
	    } else if (command.equals("/reconnect")) {
		chat.disconnectChat();
		userlistTextArea.setText("");
		messagesTextArea.setText("Disconnected");
		chat.reconnectChat();
	    } else if (command.equals("/login")) {
		handleLogin();
	    } else if (command.equals("/clearchat")) {
		getMessagesTextArea().setText("");
		messageBuffer.clear();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		} else
		    return;
	    } else 
		chat.sendMessage(data);
	} else
	    return;
    }


    public void handleLogin() {
	if (this.username != null) {
	    JOptionPane.showMessageDialog(null, "Already logged in");
	    return;
	}

	LoginDialog login = new LoginDialog();
	login.setModal(true);
	login.setVisible(true);

	String username = login.getUsername();
	String password = login.getPassword();

	if (!username.isEmpty()) {
	    chat.login(username, password);
	    getMessagesTextArea().append("You joined as " + username + "\n");
	    this.setUsername(username.toLowerCase());
	    this.user = new CytubeUser(false, username, 0);
	}
    }

    protected void handleTabComplete() {
	String[] sentence = newMessageTextField.getText().toString().split(" ");
	String partialName = sentence[sentence.length - 1].toLowerCase() + "(.*)";
	ArrayList<String> users = new ArrayList<String>();
	String replacedSentence = "";

	for (CytubeUser user : userList) {
	    if (user.getName().toLowerCase().matches(partialName)) {
		users.add(user.getName());
	    }
	}
	if (users.size() == 0)
	    return;

	if (users.size() == 1) {
	    sentence[sentence.length - 1] = users.get(0);
	    for (String word : sentence) {
		replacedSentence += word + " ";
	    }
	    newMessageTextField.setText(replacedSentence);
	} else {
	    sentence[sentence.length - 1] = this.smallestComplete(users);
	    for (String word : sentence) {
		replacedSentence += word + " ";
	    }
	    replacedSentence = 
		    replacedSentence.substring(0, replacedSentence.length() - 1);
	    newMessageTextField.setText(replacedSentence);
	}
    }


    private void NewMessageActionPerformed(java.awt.event.ActionEvent evt) {
	this.handleGUICommand(getNewMessageTextField().getText());
	getNewMessageTextField().setText(null);
    }

    private void removeUser(String username) {
	getMessagesTextArea().append(formatMessage("[Client]", username + " left the room", 
		System.currentTimeMillis(), false));
	for (CytubeUser user : userList) {
	    if (user.getName().equals(username)) {
		userList.remove(user);
		break;
	    }
	}
    }

    public void setAfk(String name, boolean afk) {
	for (CytubeUser user : userList) {
	    if (user.getName().equals(name)) {
		user.setAfk(afk);
		break;
	    }
	}
    }

    private String smallestComplete(ArrayList<String> users) {
	int[] smallestCompleteIntArray = new int[users.size()];
	String[] trimmedArray = new String[users.size()];

	for (int i = 0; i < users.size(); i++) {
	    smallestCompleteIntArray[i] = users.get(i).length();
	}

	@SuppressWarnings("rawtypes")
	List smallestCompleteIntObject = Arrays.asList(ArrayUtils.toObject(smallestCompleteIntArray));

	@SuppressWarnings("unchecked")
	int smallestCompleteInt = Collections.min(smallestCompleteIntObject);

	for (int i = 0; i < users.size(); i++) {
	    trimmedArray[i] = users.get(i).substring(0, smallestCompleteInt);
	}

	boolean changed = true;
	int maxIterations = 21;
	while (changed) {
	    changed = false;
	    String first = trimmedArray[0].toLowerCase();

	    for (int i = 0; i < trimmedArray.length; i++) {
		if (!trimmedArray[i].toLowerCase().equals(first)) {
		    changed = true;
		    break;
		}
	    }

	    if (changed) {
		for (int i = 0; i < trimmedArray.length; i++) {
		    trimmedArray[i] = trimmedArray[i]
			    .substring(0, trimmedArray[i].length() - 1);
		}
	    }

	    if (--maxIterations < 0) {
		break;
	    }
	}
	return trimmedArray[0];
    }

    private void updateUserList() {
	// Number of users. Note: I'm ignoring anons at this time
	String str = "Users: " + userList.size() + "\n-----------------\n";

	// Sort userlist
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
	userlistTextArea.setText(str);
    }

    private void privateMessage(String to, String message) throws JSONException {
	JSONObject json = new JSONObject();
	json.putOpt("to", to);
	json.putOpt("msg", message);
	json.putOpt("meta", "");

	chat.privateMessage(json);

    }

    @Override
    public void callback(JSONArray data) throws JSONException {
	// TODO Auto-generated method stub

    }

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
		this.addUser(user, true);
		this.updateUserList();
	    } else if (event.equals("userLeave")) {
		this.removeUser(obj.getString("name"));
		this.updateUserList();
	    } else if (event.equals("changeMedia")) {
		setCurrentMedia(obj.getString("title"));
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
    public void onArray(String event, JSONArray data) throws JSONException {
	// TODO Auto-generated method stub
	if (event.equals("userlist")) {
	    userList.clear();
	    JSONArray users = data.getJSONArray(0);
	    for (int i=0; i<users.length();i++) {
		boolean afk = (boolean) users.getJSONObject(i).getJSONObject("meta")
			.get("afk");
		String username = (String) users.getJSONObject(i).get("name");
		int rank = (int) users.getJSONObject(i).get("rank");
		CytubeUser user = new CytubeUser(afk, username, rank);
		addUser(user, false);
	    }
	    this.updateUserList();
	}

    }

    @Override
    public void onBoolean(String event, boolean bool) {
	if (event.equals("needPassword")) {
	    if (!roomPassword.equals("")) {
		chat.sendRoomPassword(roomPassword);
	    } else {
		String password = JOptionPane.showInputDialog("Room password");
		chat.sendRoomPassword(password);
	    }
	}
    }

    @Override
    public void onMessage(String message) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onMessage(JSONObject json) {
	// TODO Auto-generated method stub

    }

    private void onPrivateMessage(JSONObject obj) throws JSONException {
	String message = 
		this.formatMessage(obj.getString("username"), 
			obj.getString("msg"), (long) obj.get("time"), true );

	if (messageBuffer.size() > 100 && parent.isLimitChatBuffer()) {
	    messageBuffer.remove();
	    messagesTextArea.setText(messagesTextArea.getText()
		    .substring(messagesTextArea.getText().indexOf('\n')+1));
	}
	messageBuffer.add(message);
	messagesTextArea.append(message);
	messagesTextArea.setCaretPosition(messagesTextArea.getDocument().getLength());

	if (parent.getClip() != null && parent.isWindowFocus() && !parent.isUserMuteBoop()
		|| obj.getString("msg").toLowerCase().contains(getName())) {
	    parent.playSound();

	}

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {
	// TODO Auto-generated method stub

    }

    @Override
    public void onConnectFailure() {
	// TODO Auto-generated method stub

    }

    public String getCurrentMedia() {
	return currentMedia;
    }

    public void setCurrentMedia(String currentMedia) {
	this.currentMedia = currentMedia;
    }

    public JTextArea getMessagesTextArea() {
	return messagesTextArea;
    }

    public void setMessagesTextArea(JTextArea messagesTextArea) {
	this.messagesTextArea = messagesTextArea;
    }

    public JTextField getNewMessageTextField() {
	return newMessageTextField;
    }

    public void setNewMessageTextField(JTextField newMessageTextField) {
	this.newMessageTextField = newMessageTextField;
    }

    public JTextArea getUserlistTextArea() {
	return userlistTextArea;
    }

    public void setUserlistTextArea(JTextArea userlistTextArea) {
	this.userlistTextArea = userlistTextArea;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }
}