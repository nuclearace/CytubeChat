package com.milkbartube.tracy;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.JTextArea;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class CytubeRoom extends JPanel implements ChatCallbackAdapter {

    private static final long serialVersionUID = 1L;
    private JScrollPane messagesScrollPane;
    private JScrollPane newMessageScrollPane;
    private JTextField newMessageTextField;
    private JScrollPane userListScrollPane;
    private JTextPane messagesTextPane;
    private StyledDocument styledMessagesDocument;
    private JTextPane userlistTextPane;
    private StyledDocument styledUserlist;

    private Chat chat;
    private String currentMedia;
    private ChatFrame parent;
    private String room;
    private String roomPassword;
    private boolean stopMessagesAreaScrolling;
    private String username;
    private LinkedList<String> messageBuffer = new LinkedList<String>();
    private ArrayList<CytubeUser> userList = new ArrayList<CytubeUser>();
    private CytubeUser user = new CytubeUser(false, "", 0, null);
    

    public CytubeRoom(String room, String password, ChatFrame frame) {
	this.room = room;
	this.roomPassword = password;
	this.parent = frame;

	buildChatPanel();
	setChat(new Chat(this));
	getChat().start();
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
	
	userlistTextPane = new JTextPane();
	userListScrollPane.setViewportView(userlistTextPane);
	styledUserlist = userlistTextPane.getStyledDocument();

	messagesTextPane = new JTextPane();
	messagesTextPane.setEditorKit(new WrapEditorKit());
	messagesTextPane.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		int pos = messagesTextPane.viewToModel(e.getPoint());
		Element element = getStyledMessagesDocument().getCharacterElement(pos);

		System.out.println(element.getStartOffset());
		AttributeSet as = element.getAttributes();
		if (StyleConstants.getForeground(as).equals(new Color(0x351FFF))) {
		    try {
			handleLink(messagesTextPane.getText(element.getStartOffset(), 
				((element.getEndOffset() - element.getStartOffset()) - 1)));
		    } catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		    }
		}
	    }
	    @Override
	    public void mouseEntered(MouseEvent e) {
		setStopMessagesAreaScrolling(true);
	    }
	    @Override
	    public void mouseExited(MouseEvent e) {
		setStopMessagesAreaScrolling(false);
	    }
	});
	messagesTextPane.setEditable(false);
	styledMessagesDocument = messagesTextPane.getStyledDocument();
	messagesScrollPane.setViewportView(messagesTextPane);

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
		    String[] sentence = newMessageTextField.getText().toString().split(" ");
		    newMessageTextField.setText(handleTabComplete(sentence));
		}
	    }

	    @Override
	    public void keyPressed(KeyEvent e) {}

	    @Override
	    public void keyReleased(KeyEvent e) {}
	});
	setLayout(groupLayout);

    }

    private void addMessageWithLinks(ArrayList<String> list, String username, long time) {
	Date date = new Date(time);
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
	formatter.setTimeZone(TimeZone.getDefault());
	String formattedTime = formatter.format(date);

	Color color = new Color(0x351FFF);
	StyleContext sc = StyleContext.getDefaultStyleContext();
	AttributeSet attributes = sc.addAttribute(SimpleAttributeSet.EMPTY, 
		StyleConstants.Foreground, color);

	try {
	    if (!(messageBuffer.size() == 0)) {

		getStyledMessagesDocument().insertString(getStyledMessagesDocument().
			getLength(), "\n[" + formattedTime + "] " + username + ": ", null);
	    } else {
		getStyledMessagesDocument().insertString(getStyledMessagesDocument().
			getLength(), "[" + formattedTime + "] " + username + ": ", null);
	    }
	} catch (Exception e ) {}

	try {
	    for (String word : list) {
		if (!word.matches("(.*)(http(s?):/)(/[^/]+).*")) {
		    getStyledMessagesDocument().insertString(getStyledMessagesDocument().
			    getLength(), word + " ", null);
		} else {
		    getStyledMessagesDocument().insertString(getStyledMessagesDocument().
			    getLength(), word + " ", attributes);
		}
	    }
	} catch (Exception e) {}

	if (messageBuffer.size() > 100 && parent.isLimitChatBuffer()) {
	    messageBuffer.remove();
	    messagesTextPane.setText(messagesTextPane.getText()
		    .substring(messagesTextPane.getText().indexOf('\n')+1));
	}
    }

    private void addUser(CytubeUser user, boolean fromAddUser) {
	if (user.getName().toLowerCase().equals(
		this.getUser().getName().toLowerCase())) {
	    setUser(user);
	}
	if (this.getUser().getRank() <= 1  && fromAddUser) {
	    try {
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		attributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
		getStyledMessagesDocument().insertString(
			getStyledMessagesDocument().getLength(), 
			formatMessage("[Client]", 
				user.getName() + " joined the room", 
				System.currentTimeMillis()), attributes);
	    } catch (BadLocationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	if (!userList.contains(user)) {
	    userList.add(user);
	}

    }

    private void chatMsg(JSONObject obj) throws JSONException {
	ArrayList<String> list = new ArrayList<String>();
	String imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
	//String hyperlinkRegex = "<a[^>]+href\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
	String linkRegex = ".*(http(s?):/)(/[^/]+).*";
	String htmlTagRegex = "</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>";

	String cleanedString = StringEscapeUtils.unescapeHtml4(obj.getString("msg"));
	cleanedString = cleanedString.replaceAll(imgRegex, "$1");
	cleanedString = cleanedString.replaceAll(htmlTagRegex, "");
	//cleanedString = cleanedString.replaceAll(hyperlinkRegex, "$1");

	if (cleanedString.matches(linkRegex)) {
	    for (String string: cleanedString.split(" ")) {
		list.add(string);
	    }
	    addMessageWithLinks(list, 
		    obj.getString("username"), (long) obj.get("time"));
	    return;
	}

	cleanedString = 
		this.formatMessage(obj.getString("username"), 
			cleanedString, (long) obj.get("time"));

	if (messageBuffer.size() > 100 && parent.isLimitChatBuffer()) {
	    messageBuffer.remove();
	    messagesTextPane.setText(messagesTextPane.getText()
		    .substring(messagesTextPane.getText().indexOf('\n')+1));
	}

	messageBuffer.add(cleanedString);
	try {
	    getStyledMessagesDocument().insertString(getStyledMessagesDocument().
		    getLength(), messageBuffer.peekLast(), null);
	} catch (BadLocationException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	if (!isStopMessagesAreaScrolling())
	    messagesTextPane.setCaretPosition(getStyledMessagesDocument().getLength());

	try {
	    if (parent.getClip() != null && parent.isWindowFocus() && !parent.isUserMuteBoop()
		    || obj.getString("msg").toLowerCase()
		    .contains(getUsername().toLowerCase())) {
		parent.playSound();
	    }
	} catch (Exception e) {}
    }

    protected void closePMFrames() {
	for (CytubeUser user : userList) {
	    if (user.isInPrivateMessage()) {
		user.getPmFrame().setVisible(false);
	    }
	}
    }

    public String formatMessage(String username, String message, long time) {
	String cleanedString = StringEscapeUtils.unescapeHtml4(message);

	// Add the timestamp
	Date date = new Date(time);
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
	formatter.setTimeZone(TimeZone.getDefault());
	String formattedTime = formatter.format(date);

	if (!(messageBuffer.size() == 0)) {
	    message = "\n[" + formattedTime + "] ";
	} else {
	    message = "[" + formattedTime + "] ";
	}

	return message += username + ": " + cleanedString;
    }

    public void handleGUICommand(String data) {
	data = data.replace("\n", "").replace("\r", "");
	if (!data.equals("")) {
	    String[] parts = data.split(" ");
	    String command = parts[0];
	    if (command.equals("/disconnect")) {
		getChat().disconnectChat();
		userlistTextPane.setText("");
		messagesTextPane.setText("Disconnected");
	    } else if (command.equals("/login")) {
		handleLogin();
	    } else if (command.equals("/clearchat")) {
		messagesTextPane.setText("");
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
		}
	    } else if (command.equals("/grey")) {
		// Begin color prefs
		parent.changeColors(71, 77, 70, 255, 255, 255);
	    } else if (command.equals("/black")) {
		parent.changeColors(0, 0, 0, 255, 255, 255);
	    } else if (command.equals("/white")) {
		parent.changeColors(255, 255, 255, 0, 0, 0);
		// End color prefs
	    } else if (command.equals("/sound")) {
		parent.setUserMuteBoop(!parent.isUserMuteBoop());
	    } else if (command.equals("/chatbuffer")) {
		parent.setLimitChatBuffer(!parent.isLimitChatBuffer());
	    } else if (command.equals("/joinroom")) {
		parent.joinRoom();
	    } else if (command.equals("/userlist")) {
		hideUserlist();
	    } else 
		getChat().sendMessage(data);
	} else
	    return;
    }

    protected void handleLink(String uri) {
	try {
	    Desktop.getDesktop().browse(new URI(uri));
	} catch (IOException | URISyntaxException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
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
	if (username == null)
	    return;
	String password = login.getPassword();

	if (!username.isEmpty()) {
	    getChat().login(username, password);
	}
    }

    public String handleTabComplete(String[] sentence) {

	String partialName = sentence[sentence.length - 1].toLowerCase() + "(.*)";
	ArrayList<String> users = new ArrayList<String>();
	String replacedSentence = "";

	for (CytubeUser user : userList) {
	    if (user.getName().toLowerCase().matches(partialName)) {
		users.add(user.getName());
	    }
	}
	if (users.size() == 0) {
	    for (String word : sentence) {
		replacedSentence += word + " ";
	    }
	    return replacedSentence; 
	}

	if (users.size() == 1) {
	    sentence[sentence.length - 1] = users.get(0);
	    for (String word : sentence) {
		replacedSentence += word + " ";
	    }
	    return replacedSentence;
	} else {
	    sentence[sentence.length - 1] = this.smallestComplete(users);
	    for (String word : sentence) {
		replacedSentence += word + " ";
	    }
	    replacedSentence = 
		    replacedSentence.substring(0, replacedSentence.length() - 1);
	    return replacedSentence;
	}
    }

    protected void hideUserlist() {
	userListScrollPane.setVisible(!userListScrollPane.isVisible());
	parent.repaint();
    }

    private void NewMessageActionPerformed(java.awt.event.ActionEvent evt) {
	this.handleGUICommand(getNewMessageTextField().getText());
	getNewMessageTextField().setText(null);
    }

    private void removeUser(String username) {
	SimpleAttributeSet attributes = new SimpleAttributeSet();
	attributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
	try {
	    getStyledMessagesDocument().insertString(
		    getStyledMessagesDocument().getLength(), 
		    formatMessage("[Client]", username + " left the room", 
			    System.currentTimeMillis()), attributes);
	} catch (BadLocationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	for (CytubeUser user : userList) {
	    if (user.getName().equals(username)) {
		if (user.isInPrivateMessage())
		    user.getPmFrame().handleUserLeftRoom();
		userList.remove(user);
		break;
	    }
	}
    }

    private void setAfk(String name, boolean afk) {
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
	try {
	    styledUserlist.insertString(styledUserlist.getLength(), str, null);
	} catch (BadLocationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void privateMessage(String to, String message) throws JSONException {
	JSONObject json = new JSONObject();
	json.putOpt("to", to);
	json.putOpt("msg", message);
	json.putOpt("meta", "");

	getChat().privateMessage(json);
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
		CytubeUser user = new CytubeUser(afk, username, rank, this);
		this.addUser(user, true);
		this.updateUserList();
	    } else if (event.equals("userLeave")) {
		this.removeUser(obj.getString("name"));
		this.updateUserList();
	    } else if (event.equals("changeMedia")) {
		setCurrentMedia(obj.getString("title"));
		if (parent.getTabbedPane().getSelectedComponent().equals(this))
		    parent.setTitle(getCurrentMedia());
	    } else if (event.equals("pm")) {
		this.onPrivateMessage(obj);
	    } else if (event.equals("setAfk")) {
		this.setAfk(obj.getString("name"), (boolean) obj.get("afk"));
	    } else if (event.equals("login")) {
		if ((boolean) obj.get("success")) {
		    System.out.println("Logged in");
		    setUsername(obj.getString("name"));
		    user.setName(obj.getString("name"));
		} else {
		    JOptionPane.showMessageDialog(null, obj.get("error"));
		    setUsername(null);
		}
	    }
	} catch (JSONException ex) {
	    ex.printStackTrace();
	}
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
		CytubeUser user = new CytubeUser(afk, username, rank, this);
		addUser(user, false);
	    }
	    this.updateUserList();
	}
    }

    @Override
    public void onBoolean(String event, boolean bool) {
	if (event.equals("needPassword")) {
	    if (!getRoomPassword().equals("")) {
		getChat().sendRoomPassword(roomPassword);
		setRoomPassword("");
	    } else {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Enter Room Password:");
		JPasswordField pass = new JPasswordField(10);
		panel.add(label);
		panel.add(pass);
		String[] options = new String[]{"OK", "Cancel"};
		int option = JOptionPane.showOptionDialog(null, panel, "Room Password",
			JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
			null, options, options[0]);
		if (option == 0) {
		    char[] password = pass.getPassword();
		    getChat().sendRoomPassword(new String(password));
		} else {
		    parent.getTabbedPane().remove(this);
		    return;
		}
	    }
	}
    }

    @Override
    public void onMessage(String message) {}

    @Override
    public void onMessage(JSONObject json) {}

    private void onPrivateMessage(JSONObject obj) throws JSONException {
	String message = 
		this.formatMessage(obj.getString("username"), 
			obj.getString("msg"), (long) obj.get("time"));

	for (CytubeUser user : userList) {
	    if (user.getName().equals(obj.getString("username")) &&
		    !username.equals(obj.getString("username"))) {
		if (!user.isInPrivateMessage()) {
		    user.startPM(message);
		} else {
		    user.getPmFrame().addMessage(message);
		}
		break;
	    } else if (user.getName().equals(obj.getString("to"))) {
		if (!user.isInPrivateMessage()) {
		    user.startPM(message);
		} else {
		    user.getPmFrame().addMessage(message);
		}
		break;
	    }
	}

	if (parent.getClip() != null && parent.isWindowFocus() && !parent.isUserMuteBoop()
		|| obj.getString("msg").toLowerCase().contains(getName()
			.toLowerCase())) {
	    parent.playSound();
	}
    }

    @Override
    public void onConnect() {
	getChat().join(room);
    }

    @Override
    public void onDisconnect() {}

    @Override
    public void onConnectFailure() {}

    public Chat getChat() {
	return chat;
    }

    public void setChat(Chat chat) {
	this.chat = chat;
    }

    public String getCurrentMedia() {
	return currentMedia;
    }

    public void setCurrentMedia(String currentMedia) {
	this.currentMedia = currentMedia;
    }

    public JTextPane getMessagesTextPane() {
	return messagesTextPane;
    }

    public void setMessagesTextPane(JTextPane messagesTextPane) {
	this.messagesTextPane = messagesTextPane;
    }

    public JTextField getNewMessageTextField() {
	return newMessageTextField;
    }

    public void setNewMessageTextField(JTextField newMessageTextField) {
	this.newMessageTextField = newMessageTextField;
	newMessageTextField.setFocusTraversalKeysEnabled(false);
    }

    public ChatFrame getFrameParent() {
	return parent;
    }

    public void setFrameParent(ChatFrame parent) {
	this.parent = parent;
    }

    public String getRoom() {
	return room;
    }

    public void setRoom(String room) {
	this.room = room;
    }

    public String getRoomPassword() {
	return roomPassword;
    }

    public void setRoomPassword(String roomPassword) {
	this.roomPassword = roomPassword;
    }

    public boolean isStopMessagesAreaScrolling() {
	return stopMessagesAreaScrolling;
    }

    public void setStopMessagesAreaScrolling(boolean stopMessagesAreaScrolling) {
	this.stopMessagesAreaScrolling = stopMessagesAreaScrolling;
    }

    public StyledDocument getStyledMessagesDocument() {
	return styledMessagesDocument;
    }

    public void setStyledMessagesDocument(StyledDocument styledMessagesDocument) {
	this.styledMessagesDocument = styledMessagesDocument;
    }

    public CytubeUser getUser() {
	return user;
    }

    public void setUser(CytubeUser user) {
	this.user = user;
    }

    public JTextPane getUserlistTextPane() {
        return userlistTextPane;
    }

    public void setUserlistTextPane(JTextPane userlistTextPane) {
        this.userlistTextPane = userlistTextPane;
    }

    public StyledDocument getStyledUserlist() {
	return styledUserlist;
    }

    public void setStyledUserlist(StyledDocument styledUserlist) {
	this.styledUserlist = styledUserlist;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((room == null) ? 0 : room.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	CytubeRoom other = (CytubeRoom) obj;
	if (room == null) {
	    if (other.room != null)
		return false;
	} else if (!room.equals(other.room))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "ChatPanel [messagesScrollPane=" + messagesScrollPane
		+ ", messagesTextArea=" + messagesTextPane
		+ ", newMessageScrollPane=" + newMessageScrollPane
		+ ", newMessageTextField=" + newMessageTextField
		+ ", userListScrollPane=" + userListScrollPane
		+ ", userlistTextPane=" + userlistTextPane + ", chat=" + chat
		+ ", currentMedia=" + currentMedia + ", parent=" + parent.toString()
		+ ", room=" + room + ", roomPassword=" + roomPassword
		+ ", username=" + username + ", messageBuffer=" + messageBuffer
		+ ", userList=" + userList + ", user=" + user + "]";
    }
}

@SuppressWarnings("serial")
class WrapEditorKit extends StyledEditorKit {
    ViewFactory defaultFactory=new WrapColumnFactory();
    public ViewFactory getViewFactory() {
	return defaultFactory;
    }

}

class WrapColumnFactory implements ViewFactory {
    public View create(Element elem) {
	String kind = elem.getName();
	if (kind != null) {
	    if (kind.equals(AbstractDocument.ContentElementName)) {
		return new WrapLabelView(elem);
	    } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
		return new ParagraphView(elem);
	    } else if (kind.equals(AbstractDocument.SectionElementName)) {
		return new BoxView(elem, View.Y_AXIS);
	    } else if (kind.equals(StyleConstants.ComponentElementName)) {
		return new ComponentView(elem);
	    } else if (kind.equals(StyleConstants.IconElementName)) {
		return new IconView(elem);
	    }
	}

	// default to text display
	return new LabelView(elem);
    }
}

class WrapLabelView extends LabelView {
    public WrapLabelView(Element elem) {
	super(elem);
    }

    public float getMinimumSpan(int axis) {
	switch (axis) {
	case View.X_AXIS:
	    return 0;
	case View.Y_AXIS:
	    return super.getMinimumSpan(axis);
	default:
	    throw new IllegalArgumentException("Invalid axis: " + axis);
	}
    }

}
