package com.milkbartube.tracy;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

public class ChatFrame extends JFrame implements ChatCallbackAdapter, WindowFocusListener {

    private static final long serialVersionUID = -3120953406569989166L;

    // Begin variables
    private JButton btnLogin;
    private JMenuBar menuBar;
    private JMenu mnMenu;
    private JScrollPane messagesScrollPane;
    private JTextArea messagesTextArea;
    private JScrollPane newMessageScrollPane;
    private JTextField newMessageTextField;
    private String roomPassword;
    private JScrollPane userListScrollPane;
    private JTextArea userListTextArea;

    private Chat chat;
    private Clip clip;
    private boolean limitChatBuffer = false;
    private LinkedList<String> messageBuffer = new LinkedList<String>();
    private ArrayList<CytubeUser> userList = new ArrayList<CytubeUser>();
    private CytubeUser user = new CytubeUser(false, "", 0);
    private boolean userMuteBoop = true;
    private String userName;
    private boolean windowFocus = false;
    // End variables

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

	messagesScrollPane = new JScrollPane();
	messagesTextArea = new JTextArea();
	userListScrollPane = new JScrollPane();
	userListTextArea = new JTextArea();
	newMessageScrollPane = new JScrollPane();
	newMessageTextField = new JTextField();

	setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

	messagesTextArea.setEditable(false);
	messagesTextArea.setColumns(20);
	messagesTextArea.setLineWrap(true);
	messagesTextArea.setRows(5);
	messagesScrollPane.setViewportView(messagesTextArea);

	menuBar = new JMenuBar();
	menuBar.setBackground(Color.DARK_GRAY);
	setJMenuBar(menuBar);

	mnMenu = new JMenu("Menu");
	mnMenu.setBackground(Color.DARK_GRAY);
	menuBar.add(mnMenu);

	btnLogin = new JButton("Login");
	btnLogin.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		handleLogin();
	    }
	});
	mnMenu.add(btnLogin);

	JButton btnDisconnect = new JButton("Disconnect");
	btnDisconnect.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		chat.disconnectChat();
		userListTextArea.setText("");
		setTitle("Disconnected!");
		setUserName(null);
	    }
	});
	mnMenu.add(btnDisconnect);

	JButton btnReconnect = new JButton("Reconnect");
	btnReconnect.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		chat.disconnectChat();
		setUserName("");
		userListTextArea.setText("");
		messagesTextArea.setText("Connecting...");
		chat.reconnectChat();
		setUserName(null);
	    }
	});
	mnMenu.add(btnReconnect);

	JButton btnQuit = new JButton("Quit");
	btnQuit.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		System.exit(0);
	    }
	});
	mnMenu.add(btnQuit);

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

	newMessageTextField.setBorder(null);
	newMessageTextField.setFocusTraversalKeysEnabled(false);
	newMessageTextField.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		NewMessageActionPerformed(evt);
	    }
	});
	newMessageTextField.addKeyListener(new KeyListener() {
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
	addWindowFocusListener(this);
	newMessageScrollPane.setViewportView(newMessageTextField);

	javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	layout.setHorizontalGroup(
		layout.createParallelGroup(Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(newMessageScrollPane, GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
					.addComponent(userListScrollPane, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(messagesScrollPane, GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)))
					.addContainerGap())
		);
	layout.setVerticalGroup(
		layout.createParallelGroup(Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(userListScrollPane, GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
				.addComponent(messagesScrollPane, GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(newMessageScrollPane, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
				.addContainerGap())
		);
	getContentPane().setLayout(layout);

	pack();
    }

    private void NewMessageActionPerformed(java.awt.event.ActionEvent evt) {
	this.handleGUICommand(newMessageTextField.getText());
	newMessageTextField.setText(null);
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
	messagesTextArea.append("Connecting...");
	chat = new Chat(this);
	chat.start();
    }

    public void disableNewMessages() {
	newMessageTextField.setEnabled(false);
    }

    public void enableNewMessages() {
	newMessageTextField.setEnabled(true);
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
		setUserName(null);
	    } else if (command.equals("/reconnect")) {
		chat.disconnectChat();
		setUserName(null);
		userListTextArea.setText("");
		messagesTextArea.setText("Connecting...");
		chat.reconnectChat();
	    } else if (command.equals("/login")) {
		this.handleLogin();
	    } else if (command.equals("/grey")) {
		// Begin color prefs
		this.changeColors(71, 77, 70, 255, 255, 255);
	    } else if (command.equals("/black")) {
		this.changeColors(0, 0, 0, 255, 255, 255);
	    } else if (command.equals("/white")) {
		this.changeColors(255, 255, 255, 0, 0, 0);
		// End color prefs
	    } else if (command.equals("/clearchat")) {
		messagesTextArea.setText("");
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
			e.printStackTrace();
		    }
		} else
		    return;
	    } else if (command.equals("/sound")) {
		this.setUserMuteBoop(!this.isUserMuteBoop());
	    } else if (command.equals("/chatbuffer")) {
		this.setLimitChatBuffer(!this.isLimitChatBuffer());
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
		this.addUser(user, true);
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
	messagesTextArea.append("done!\n*Type /login to login*\n");
	enableNewMessages();

	RoomDialog roomInput = new RoomDialog();
	roomInput.setModal(true);
	roomInput.setVisible(true);

	String room = roomInput.getRoom();
	roomPassword = roomInput.getPassword();

	if (!room.isEmpty()) {
	    chat.join(room);
	}
    }

    @Override
    public void onDisconnect() {
	messagesTextArea.setText("Disconnected!\n");
    }

    @Override
    public void onConnectFailure() {
	messagesTextArea.append("error!\n");
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
		this.addUser(user, false);
	    }
	    this.updateUserList();
	}
    }

    @Override
    public void onBoolean(String event, boolean bool) {
	// TODO Auto-generated method stub
	if (event.equals("needPassword")) {
	    if (!roomPassword.equals("")) {
		chat.sendRoomPassword(roomPassword);
	    } else {
		String password = JOptionPane.showInputDialog("Room password");
		chat.sendRoomPassword(password);
	    }
	}

    }

    public void addUser(CytubeUser user, boolean fromAddUser) {
	if (user.getName().toLowerCase().equals(
		this.user.getName().toLowerCase())) {
	    this.user = user;
	}
	if (this.user.getRank() <= 1  && fromAddUser) {
	    messagesTextArea.append(formatMessage("[Client]", 
		    user.getName() + " joined the room", 
		    System.currentTimeMillis(), false));
	}
	if (!userList.contains(user)) {
	    userList.add(user);
	}
    }

    public void changeColors(int r1, int g1, int b1, int r2, int g2, int b2) {
	messagesTextArea.setBackground(new java.awt.Color(r1, g1, b1));
	messagesTextArea.setForeground(new java.awt.Color(r2, g2, b2));

	userListTextArea.setBackground(new java.awt.Color(r1, g1, b1));
	userListTextArea.setForeground(new java.awt.Color(r2, g2, b2));

	newMessageTextField.setBackground(new java.awt.Color(r1, g1, b1));
	newMessageTextField.setForeground(new java.awt.Color(r2, g2, b2));
    }

    public void changeMedia(String media) {
	setTitle("Now Playing: " + media);
    }

    public void chatMsg(JSONObject obj) throws JSONException {
	String message = 
		this.formatMessage(obj.getString("username"), 
			obj.getString("msg"), (long) obj.get("time"), false);

	if (messageBuffer.size() > 100 && isLimitChatBuffer()) {
	    messageBuffer.remove();
	    messagesTextArea.setText(messagesTextArea.getText()
		    .substring(messagesTextArea.getText().indexOf('\n')+1));
	}

	messageBuffer.add(message);
	messagesTextArea.append(message);
	messagesTextArea.setCaretPosition(messagesTextArea.getDocument().getLength());

	if (this.clip != null && this.isWindowFocus() && !this.userMuteBoop
		|| obj.getString("msg").toLowerCase().contains(this.getUserName())) {
	    this.playSound();
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

    public String getPassword() {
	JPanel panel = new JPanel();
	JLabel label = new JLabel("Enter a password:");
	JPasswordField passwordField = new JPasswordField(20);
	panel.add(label);
	panel.add(passwordField);
	String[] options = new String[]{"OK", "Guest Login"};
	int x = JOptionPane.showOptionDialog(null, panel, "Enter Password",
		JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
		null, options, options[0]);
	if (x == 0) {
	    char[] passwordCharArray = passwordField.getPassword();
	    return new String(passwordCharArray);
	}
	return null;
    }

    public void handleTabComplete() {
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

    public void handleLogin() {
	if (this.userName != null) {
	    JOptionPane.showMessageDialog(null, "Already logged in");
	    return;
	}
	String username = JOptionPane.showInputDialog(null, "Nickname", null, WIDTH);
	String password = this.getPassword();

	if (!username.isEmpty()) {
	    chat.login(username, password);
	    messagesTextArea.append("You joined as " + username + "\n");
	    this.setUserName(username.toLowerCase());
	    this.user = new CytubeUser(false, username, 0);
	}
    }

    public boolean isLimitChatBuffer() {
	return limitChatBuffer;
    }

    public void setLimitChatBuffer(boolean limitChatBuffer) {
	this.limitChatBuffer = limitChatBuffer;
    }

    public void onPrivateMessage(JSONObject obj) throws JSONException {
	String message = 
		this.formatMessage(obj.getString("username"), 
			obj.getString("msg"), (long) obj.get("time"), true );

	if (messageBuffer.size() > 100 && isLimitChatBuffer()) {
	    messageBuffer.remove();
	    messagesTextArea.setText(messagesTextArea.getText()
		    .substring(messagesTextArea.getText().indexOf('\n')+1));
	}
	messageBuffer.add(message);
	messagesTextArea.append(message);
	messagesTextArea.setCaretPosition(messagesTextArea.getDocument().getLength());

	if (this.clip != null && this.isWindowFocus() && !this.userMuteBoop
		|| obj.getString("msg").toLowerCase().contains(this.getUserName())) {
	    this.playSound();

	}
    }

    public void playSound() {
	this.clip.start();
	this.clip.setFramePosition(0);
    }

    public void privateMessage(String to, String message) throws JSONException {
	JSONObject json = new JSONObject();
	json.putOpt("to", to);
	json.putOpt("msg", message);
	json.putOpt("meta", "");

	chat.privateMessage(json);
    }

    public void removeUser(String username) {
	messagesTextArea.append(formatMessage("[Client]", username + " left the room", 
		System.currentTimeMillis(), false));
	for (CytubeUser user : userList) {
	    if (user.getName().equals(username)) {
		userList.remove(user);
		break;
	    }
	}
    }

    @SuppressWarnings("unchecked")
    public String smallestComplete(ArrayList<String> users) {
	int[] smallestCompleteIntArray = new int[users.size()];
	String[] trimmedArray = new String[users.size()];

	for (int i = 0; i < users.size(); i++) {
	    smallestCompleteIntArray[i] = users.get(i).length();
	}

	@SuppressWarnings("rawtypes")
	List smallestCompleteIntObject = Arrays.asList(ArrayUtils.toObject(smallestCompleteIntArray));

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

    public boolean isWindowFocus() {
	return windowFocus;
    }

    public void setWindowFocus(boolean windowFocus) {
	this.windowFocus = windowFocus;
    }

    public void setAfk(String name, boolean afk) {
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
	userListTextArea.setText(str);
    }

    public boolean isUserMuteBoop() {
	return userMuteBoop;
    }

    public void setUserMuteBoop(boolean userMuteBoop) {
	this.userMuteBoop = userMuteBoop;
    }

    public String getUserName() {
	return userName;
    }

    public void setUserName(String userName) {
	this.userName = userName;
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
	this.windowFocus = false;
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
	this.windowFocus = true;
    }
}