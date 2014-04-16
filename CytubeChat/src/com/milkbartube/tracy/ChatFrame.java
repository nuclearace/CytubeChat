package com.milkbartube.tracy;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

public class ChatFrame extends JFrame implements ChatCallbackAdapter, WindowFocusListener {

    private static final long serialVersionUID = -3120953406569989166L;
    private JMenuBar menuBar;
    private JMenu mnMenu;
    private JPanel chatPane;
    private JMenuItem mntmLogin;
    private JMenuItem mntmDisconnect;
    private JMenuItem mntmReconnect;
    private JMenuItem mntmQuit;
    private JTabbedPane tabbedPane;

    private Chat chat;
    private Clip clip;
    private boolean limitChatBuffer = false;
    private boolean userMuteBoop = true;
    private String username;
    private String roomPassword;
    private boolean windowFocus = false;
    private JMenuItem mntmJoinRoom;
    // End variables

    public ChatFrame() {

	initComponents();
	setVisible(true);
	setLocationRelativeTo(null);
	try {
	    URL soundFile = new URL("http://cytu.be/boop.wav");
	    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);

	    this.setClip(AudioSystem.getClip());
	    getClip().open(audioIn);
	} catch (Exception e) {
	    this.setClip(null);
	    e.printStackTrace();
	}

	startChat();
    }

    private void initComponents() {

	setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

	menuBar = new JMenuBar();
	menuBar.setBackground(Color.WHITE);
	setJMenuBar(menuBar);

	mnMenu = new JMenu("Menu");
	mnMenu.setBackground(Color.WHITE);
	menuBar.add(mnMenu);

	mntmLogin = new JMenuItem("Login");
	mntmLogin.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_L, (Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask())));
	mntmLogin.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		handleLogin();
	    }
	});
	mnMenu.add(mntmLogin);

	mntmDisconnect = new JMenuItem("Disconnect");
	mntmDisconnect.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_D, (Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask())));
	mntmDisconnect.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		chat.disconnectChat();
		int totalTabs = tabbedPane.getTabCount();
		for(int i = 0; i < totalTabs; i++)
		{
		    ChatPanel c = (ChatPanel) tabbedPane.getTabComponentAt(i);
		    c.getUserlistTextArea().setText("");
		    c.getMessagesTextArea().setText("Disconnected");
		}
		setTitle("Disconnected!");
		setUserName(null);
	    }
	});

	mntmJoinRoom = new JMenuItem("Join Room");
	mntmJoinRoom.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_J, (Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask())));
	mntmJoinRoom.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		joinRoom();
	    }
	});
	mnMenu.add(mntmJoinRoom);
	mnMenu.add(mntmDisconnect);

	mntmReconnect = new JMenuItem("Reconnect");
	mntmReconnect.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_R, (Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask())));
	mntmReconnect.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		chat.disconnectChat();
		setUserName(null);
		int totalTabs = tabbedPane.getTabCount();
		for(int i = 0; i < totalTabs; i++)
		{
		    ChatPanel c = (ChatPanel) tabbedPane.getTabComponentAt(i);
		    c.getUserlistTextArea().setText("");
		    c.getMessagesTextArea().setText("Disconnected");
		}
		chat.reconnectChat();
	    }
	});
	mnMenu.add(mntmReconnect);

	mntmQuit = new JMenuItem("Quit");
	mntmQuit.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_Q, (Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask())));
	mntmQuit.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		System.exit(0);
	    }
	});
	mnMenu.add(mntmQuit);

	addWindowFocusListener(this);

	tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

	javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	layout.setHorizontalGroup(
		layout.createParallelGroup(Alignment.LEADING)
		.addComponent(tabbedPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
		);
	layout.setVerticalGroup(
		layout.createParallelGroup(Alignment.LEADING)
		.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
		);
	getContentPane().setLayout(layout);

	pack();
    }

    public void startChat() {
	chat = new Chat(this);
	chat.start();
    }

    public void handleGUICommand(String data) {
	data = data.replace("\n", "").replace("\r", "");
	if (!data.equals("")) {
	    String[] parts = data.split(" ");
	    String command = parts[0];
	    if (command.equals("/disconnect")) {
		chat.disconnectChat();
		int totalTabs = tabbedPane.getTabCount();
		for(int i = 0; i < totalTabs; i++)
		{
		    ChatPanel c = (ChatPanel) tabbedPane.getTabComponentAt(i);
		    c.getUserlistTextArea().setText("");
		    c.getMessagesTextArea().setText("Disconnected");
		}
		setTitle("Disconnected!");
		setUserName(null);
	    } else if (command.equals("/reconnect")) {
		chat.disconnectChat();
		setUserName(null);
		int totalTabs = tabbedPane.getTabCount();
		for(int i = 0; i < totalTabs; i++)
		{
		    ChatPanel c = (ChatPanel) tabbedPane.getTabComponentAt(i);
		    c.getUserlistTextArea().setText("");
		    c.getMessagesTextArea().setText("Disconnected");
		}
		chat.reconnectChat();
	    } else if (command.equals("/grey")) {
		// Begin color prefs
		this.changeColors(71, 77, 70, 255, 255, 255);
	    } else if (command.equals("/black")) {
		this.changeColors(0, 0, 0, 255, 255, 255);
	    } else if (command.equals("/white")) {
		this.changeColors(255, 255, 255, 0, 0, 0);
		// End color prefs
	    } else if (command.equals("/sound")) {
		this.setUserMuteBoop(!this.isUserMuteBoop());
	    } else if (command.equals("/chatbuffer")) {
		this.setLimitChatBuffer(!this.isLimitChatBuffer());
	    } else if (command.equals("/joinroom")) {
		joinRoom();
	    } else 
		chat.sendMessage(data);
	} else
	    return;
    }

    @Override
    public void callback(JSONArray data) throws JSONException {}

    @Override
    public void on(String event, JSONObject obj) {
	//	try {
	//	    if (event.equals("chatMsg")) {
	//		this.chatMsg(obj);
	//	    } else if (event.equals("addUser")) {
	//		boolean afk = (boolean) obj.getJSONObject("meta").get("afk");
	//		String username = obj.getString("name") ;
	//		int rank = (int) obj.get("rank");
	//		CytubeUser user = new CytubeUser(afk, username, rank);
	//		this.addUser(user, true);
	//		this.updateUserList();
	//	    } else if (event.equals("userLeave")) {
	//		this.removeUser(obj.getString("name"));
	//		this.updateUserList();
	//	    } else if (event.equals("changeMedia")) {
	//		this.changeMedia(obj.getString("title"));
	//	    } else if (event.equals("pm")) {
	//		this.onPrivateMessage(obj);
	//	    } else if (event.equals("setAfk")) {
	//		this.setAfk(obj.getString("name"), (boolean) obj.get("afk"));
	//	    }
	//	} catch (JSONException ex) {
	//	    ex.printStackTrace();
	//	}
    }

    @Override
    public void onMessage(String message) {}

    @Override
    public void onMessage(JSONObject json) {}

    @Override
    public void onConnect() {
	joinRoom();
    }

    @Override
    public void onDisconnect() {
	//messagesTextArea.setText("Disconnected!\n");
    }

    @Override
    public void onConnectFailure() {
	int totalTabs = tabbedPane.getTabCount();
	for(int i = 0; i < totalTabs; i++)
	{
	    ChatPanel c = (ChatPanel) tabbedPane.getTabComponentAt(i);
	    c.getUserlistTextArea().setText("");
	    c.getMessagesTextArea().setText("error!\n");
	}

    }

    @Override
    public void onArray(String event, JSONArray data) throws JSONException {}

    @Override
    public void onBoolean(String event, boolean bool) {
	//	if (event.equals("needPassword")) {
	//	    if (!roomPassword.equals("")) {
	//		chat.sendRoomPassword(roomPassword);
	//	    } else {
	//		String password = JOptionPane.showInputDialog("Room password");
	//		chat.sendRoomPassword(password);
	//	    }
	//	}
    }

    //    public void addUser(CytubeUser user, boolean fromAddUser) {
    //	if (user.getName().toLowerCase().equals(
    //		this.user.getName().toLowerCase())) {
    //	    this.user = user;
    //	}
    //	if (this.user.getRank() <= 1  && fromAddUser) {
    //	    messagesTextArea.append(formatMessage("[Client]", 
    //		    user.getName() + " joined the room", 
    //		    System.currentTimeMillis(), false));
    //	}
    //	if (!userList.contains(user)) {
    //	    userList.add(user);
    //	}
    //    }

    public void changeColors(int r1, int g1, int b1, int r2, int g2, int b2) {
	int totalTabs = tabbedPane.getTabCount();
	for(int i = 0; i < totalTabs; i++)
	{
	    ChatPanel c = (ChatPanel) tabbedPane.getTabComponentAt(i);
	    System.out.println("Got pane " + c.toString());

	    c.getMessagesTextArea().setBackground(new java.awt.Color(r1, g1, b1));
	    c.getMessagesTextArea().setForeground(new java.awt.Color(r2, g2, b2));

	    c.getUserlistTextArea().setBackground(new java.awt.Color(r1, g1, b1));
	    c.getUserlistTextArea().setForeground(new java.awt.Color(r2, g2, b2));

	    c.getNewMessageTextField().setBackground(new java.awt.Color(r1, g1, b1));
	    c.getNewMessageTextField().setForeground(new java.awt.Color(r2, g2, b2));
	}
    }

    public void changeMedia(String media) {
	setTitle("Now Playing: " + media);
    }

    public void chatMsg(JSONObject obj) throws JSONException {
	//	String message = 
	//		this.formatMessage(obj.getString("username"), 
	//			obj.getString("msg"), (long) obj.get("time"), false);
	//
	//	if (messageBuffer.size() > 100 && isLimitChatBuffer()) {
	//	    messageBuffer.remove();
	//	    messagesTextArea.setText(messagesTextArea.getText()
	//		    .substring(messagesTextArea.getText().indexOf('\n')+1));
	//	}
	//
	//	messageBuffer.add(message);
	//	messagesTextArea.append(message);
	//	messagesTextArea.setCaretPosition(messagesTextArea.getDocument().getLength());
	//
	//	if (this.getClip() != null && this.isWindowFocus() && !this.userMuteBoop
	//		|| obj.getString("msg").toLowerCase().contains(this.getUserName())) {
	//	    this.playSound();
	//	}
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

    //    public void handleTabComplete() {
    //	String[] sentence = newMessageTextField.getText().toString().split(" ");
    //	String partialName = sentence[sentence.length - 1].toLowerCase() + "(.*)";
    //	ArrayList<String> users = new ArrayList<String>();
    //	String replacedSentence = "";
    //
    //	for (CytubeUser user : userList) {
    //	    if (user.getName().toLowerCase().matches(partialName)) {
    //		users.add(user.getName());
    //	    }
    //	}
    //	if (users.size() == 0)
    //	    return;
    //
    //	if (users.size() == 1) {
    //	    sentence[sentence.length - 1] = users.get(0);
    //	    for (String word : sentence) {
    //		replacedSentence += word + " ";
    //	    }
    //	    newMessageTextField.setText(replacedSentence);
    //	} else {
    //	    sentence[sentence.length - 1] = this.smallestComplete(users);
    //	    for (String word : sentence) {
    //		replacedSentence += word + " ";
    //	    }
    //	    replacedSentence = 
    //		    replacedSentence.substring(0, replacedSentence.length() - 1);
    //	    newMessageTextField.setText(replacedSentence);
    //	}
    //    }

    public void handleLogin() {
	ChatPanel panel = 
		(ChatPanel) tabbedPane.getSelectedComponent();
	if (panel.getUsername() != null) {
	    JOptionPane.showMessageDialog(null, "Already logged in");
	    return;
	}

	LoginDialog login = new LoginDialog();
	login.setModal(true);
	login.setVisible(true);

	String username = login.getUsername();
	String password = login.getPassword();

	if (!username.isEmpty()) {
	    panel.getChat().login(username, password);
	    panel.setUsername(username.toLowerCase());
	    panel.setUser(new CytubeUser(false, username, 0));
	}
    }

    public boolean isLimitChatBuffer() {
	return limitChatBuffer;
    }

    public void setLimitChatBuffer(boolean limitChatBuffer) {
	this.limitChatBuffer = limitChatBuffer;
    }

    public void joinRoom() {
	RoomDialog roomInput = new RoomDialog();
	roomInput.setModal(true);
	roomInput.setVisible(true);

	String room = roomInput.getRoom();
	roomPassword = roomInput.getPassword();

	if (!room.isEmpty()) {
	    tabbedPane.addTab(room, 
		    new ChatPanel(username, room, roomPassword, this));
	}
    }

    //    public void onPrivateMessage(JSONObject obj) throws JSONException {
    //	String message = 
    //		this.formatMessage(obj.getString("username"), 
    //			obj.getString("msg"), (long) obj.get("time"), true );
    //
    //	if (messageBuffer.size() > 100 && isLimitChatBuffer()) {
    //	    messageBuffer.remove();
    //	    messagesTextArea.setText(messagesTextArea.getText()
    //		    .substring(messagesTextArea.getText().indexOf('\n')+1));
    //	}
    //	messageBuffer.add(message);
    //	messagesTextArea.append(message);
    //	messagesTextArea.setCaretPosition(messagesTextArea.getDocument().getLength());
    //
    //	if (this.getClip() != null && this.isWindowFocus() && !this.userMuteBoop
    //		|| obj.getString("msg").toLowerCase().contains(this.getUserName())) {
    //	    this.playSound();
    //
    //	}
    //    }

    public void playSound() {
	this.getClip().start();
	this.getClip().setFramePosition(0);
    }

    //    public void privateMessage(String to, String message) throws JSONException {
    //	JSONObject json = new JSONObject();
    //	json.putOpt("to", to);
    //	json.putOpt("msg", message);
    //	json.putOpt("meta", "");
    //
    //	chat.privateMessage(json);
    //    }

    //    public void removeUser(String username) {
    //	messagesTextArea.append(formatMessage("[Client]", username + " left the room", 
    //		System.currentTimeMillis(), false));
    //	for (CytubeUser user : userList) {
    //	    if (user.getName().equals(username)) {
    //		userList.remove(user);
    //		break;
    //	    }
    //	}
    //    }

    //    @SuppressWarnings("unchecked")
    //    public String smallestComplete(ArrayList<String> users) {
    //	int[] smallestCompleteIntArray = new int[users.size()];
    //	String[] trimmedArray = new String[users.size()];
    //
    //	for (int i = 0; i < users.size(); i++) {
    //	    smallestCompleteIntArray[i] = users.get(i).length();
    //	}
    //
    //	@SuppressWarnings("rawtypes")
    //	List smallestCompleteIntObject = Arrays.asList(ArrayUtils.toObject(smallestCompleteIntArray));
    //
    //	int smallestCompleteInt = Collections.min(smallestCompleteIntObject);
    //
    //	for (int i = 0; i < users.size(); i++) {
    //	    trimmedArray[i] = users.get(i).substring(0, smallestCompleteInt);
    //	}
    //
    //	boolean changed = true;
    //	int maxIterations = 21;
    //	while (changed) {
    //	    changed = false;
    //	    String first = trimmedArray[0].toLowerCase();
    //
    //	    for (int i = 0; i < trimmedArray.length; i++) {
    //		if (!trimmedArray[i].toLowerCase().equals(first)) {
    //		    changed = true;
    //		    break;
    //		}
    //	    }
    //
    //	    if (changed) {
    //		for (int i = 0; i < trimmedArray.length; i++) {
    //		    trimmedArray[i] = trimmedArray[i]
    //			    .substring(0, trimmedArray[i].length() - 1);
    //		}
    //	    }
    //
    //	    if (--maxIterations < 0) {
    //		break;
    //	    }
    //	}
    //	return trimmedArray[0];
    //    }

    public boolean isWindowFocus() {
	return windowFocus;
    }

    public void setWindowFocus(boolean windowFocus) {
	this.windowFocus = windowFocus;
    }

    //    public void setAfk(String name, boolean afk) {
    //	for (CytubeUser user : userList) {
    //	    if (user.getName().equals(name)) {
    //		user.setAfk(afk);
    //		break;
    //	    }
    //	}
    //    }

    //    public void updateUserList() {
    //	// Number of users. Note: I'm ignoring anons at this time
    //	String str = "Users: " + userList.size() + "\n-----------------\n";
    //
    //	// Sort userlist
    //	Collections.sort(userList, new Comparator<CytubeUser>() {
    //	    @Override
    //	    public int compare(CytubeUser user1, CytubeUser user2) {
    //		return user1.getName().compareToIgnoreCase(user2.getName());
    //	    }
    //	});
    //	for (CytubeUser user : userList) {
    //	    switch (user.getRank()) {
    //	    case 2:
    //		str += "~" + user.getName() + "\n";
    //		break;
    //	    case 3:
    //		str += "@" + user.getName() + "\n";
    //		break;
    //	    case 4:
    //		str += "@" + user.getName() + "\n";
    //		break;
    //	    case 5:
    //		str += "$" + user.getName() + "\n";
    //		break;
    //	    case 255:
    //		str += "%" + user.getName() + "\n";
    //		break;
    //	    default:
    //		str += user.getName() + "\n";
    //		break;
    //	    }
    //	}
    //	userListTextArea.setText(str);
    //    }

    public boolean isUserMuteBoop() {
	return userMuteBoop;
    }

    public void setUserMuteBoop(boolean userMuteBoop) {
	this.userMuteBoop = userMuteBoop;
    }

    public String getUserName() {
	return username;
    }

    public void setUserName(String userName) {
	this.username = userName;
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
	this.windowFocus = false;
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
	this.windowFocus = true;
    }

    public Clip getClip() {
	return clip;
    }

    public void setClip(Clip clip) {
	this.clip = clip;
    }

    public JTabbedPane getTabbedPane() {
	return tabbedPane;
    }

    public void setTabbedPane(JTabbedPane tabbedPane) {
	this.tabbedPane = tabbedPane;
    }
}