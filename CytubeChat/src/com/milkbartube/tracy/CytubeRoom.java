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
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.ArrayUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private CytubePlaylist playlistFrame;

    private Chat chat;
    private String currentMedia;
    private ChatFrame parent;
    private LinkedList<CytubeVideo> playlist = new LinkedList<CytubeVideo>();
    private String room;
    private String roomPassword;
    private String server;
    private boolean stopMessagesAreaScrolling;
    private String username;
    private LinkedList<String> messageBuffer = new LinkedList<String>();
    private ArrayList<CytubeUser> userList = new ArrayList<CytubeUser>();
    private CytubeUser user = new CytubeUser(false, "", 0, null, false);

    public CytubeRoom(String room, String password, ChatFrame frame, String socketURL) {
	this.room = room;
	this.roomPassword = password;
	this.parent = frame;
	this.server = socketURL;

	buildChatPanel();
	setChat(new Chat(this, server));
    }

    /**
     * Create the panel.
     */
    private void buildChatPanel() {

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
	userlistTextPane.setEditable(false);
	userListScrollPane.setViewportView(userlistTextPane);
	styledUserlist = userlistTextPane.getStyledDocument();

	messagesTextPane = new JTextPane();
	messagesTextPane.setEditorKit(new WrapEditorKit());
	messagesTextPane.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		int pos = messagesTextPane.viewToModel(e.getPoint());
		Element element = getStyledMessagesDocument().getCharacterElement(pos);

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

    private void addUser(CytubeUser user, boolean fromAddUser) throws BadLocationException {
	if (user.getUsername().toLowerCase().equals(
		this.getUser().getUsername().toLowerCase())) {
	    setUser(user);
	}
	if (this.getUser().getRank() <= 1  && fromAddUser) {
	    SimpleAttributeSet attributes = new SimpleAttributeSet();
	    attributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
	    getStyledMessagesDocument().insertString(
		    getStyledMessagesDocument().getLength(), 
		    CytubeUtils.formatMessage("[Client]", 
			    user.getUsername() + " joined the room", 
			    System.currentTimeMillis()), attributes);
	    if (!isStopMessagesAreaScrolling())
		messagesTextPane.setCaretPosition(getStyledMessagesDocument().getLength());
	}
	if (!userList.contains(user)) {
	    userList.add(user);
	}
    }

    private void addVideo(JSONObject obj, boolean isMove, int uid, CytubeVideo movedVideo) 
	    throws JSONException, BadLocationException {
	int posInPlaylist = 0;
	if (isMove) {
	    for (int i = 0; i <playlist.size(); i++) {
		if (playlist.get(i).getUid() == uid) {
		    posInPlaylist = i + 1;
		    break;
		}
	    }
	    playlist.add(posInPlaylist, movedVideo);
	    return;
	}
	JSONObject item = obj.getJSONObject("item");
	CytubeVideo video = new CytubeVideo(item);

	for (int i = 0; i < playlist.size(); i++) {
	    if (playlist.get(i).getUid() == obj.getInt("after")) {
		posInPlaylist = i + 1;
		break;
	    }
	}
	playlist.add(posInPlaylist, video);

	if (getPlaylistFrame() != null) {
	    getPlaylistFrame().setPlaylist(playlist);
	    getPlaylistFrame().drawPlaylist();
	}
    }

    private void chatMsg(JSONObject obj) throws JSONException, BadLocationException {
	ArrayList<String> list = new ArrayList<String>();
	Pattern linkPattern = Pattern.compile("(\\w+:\\/\\/(?:[^:\\/\\[\\]\\s]+|\\[[0-9a-f:]+\\])(?::\\d+)?(?:\\/[^\\/\\s]*)*)");

	for (CytubeUser user : userList) {
	    if (user.getUsername().equalsIgnoreCase(obj.getString("username"))
		    && user.isIgnore()) {
		return;
	    }
	}

	String cleanedString = CytubeUtils.formatMessage(obj.getString("username"), 
		obj.getString("msg"), (long) obj.get("time"));

	Matcher matcher = linkPattern.matcher(cleanedString);

	if (matcher.find()) {
	    for (String word: cleanedString.split(" ")) {
		list.add(word);
	    }
	    CytubeUtils.addMessageWithLinks(list, false, getStyledMessagesDocument(), this);

	    if (getFrameParent().getClip() != null && getFrameParent().isWindowFocus() 
		    && !getFrameParent().isUserMuteBoop()
		    || getUsername() != null && cleanedString.toLowerCase()
		    .contains(getUsername().toLowerCase())) {
		getFrameParent().playSound();
	    }
	    return;
	}

	cleanedString = 
		CytubeUtils.formatMessage(obj.getString("username"), 
			obj.getString("msg"), (long) obj.get("time"));

	if (getMessageBuffer().size() > 100 && getFrameParent().isLimitChatBuffer()) {
	    getMessageBuffer().remove();
	    getMessagesTextPane().setText(getMessagesTextPane().getText()
		    .substring(getMessagesTextPane().getText().indexOf('\n')+1));
	}

	getMessageBuffer().add(cleanedString);
	getStyledMessagesDocument().insertString(getStyledMessagesDocument().
		getLength(), getMessageBuffer().peekLast(), null);

	if (!stopMessagesAreaScrolling)
	    getMessagesTextPane().setCaretPosition(getStyledMessagesDocument().getLength());

	if (getFrameParent().getClip() != null && getFrameParent().isWindowFocus() 
		&& !getFrameParent().isUserMuteBoop()
		|| getUsername() != null && cleanedString.toLowerCase()
		.contains(getUsername().toLowerCase()) 
		&& getFrameParent().isWindowFocus() ) {
	    getFrameParent().playSound();
	}
    }

    protected void closePMFrames() {
	for (CytubeUser user : userList) {
	    if (user.isInPrivateMessage()) {
		user.getPmFrame().setVisible(false);
	    }
	}
    }

    private CytubeVideo deleteVideo(int uid, boolean isMove) throws BadLocationException {
	for (int i = 0; i < playlist.size(); i++) {
	    if (playlist.get(i).getUid() == uid) {
		CytubeVideo removedVideo = playlist.remove(i);

		if (getPlaylistFrame() != null && !isMove) {
		    getPlaylistFrame().setPlaylist(playlist);
		    getPlaylistFrame().drawPlaylist();
		}
		return removedVideo;
	    }
	}
	return null;
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
		getMessageBuffer().clear();
	    } else if (command.equals("/pm")) {
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
	    } else if (command.equals("/ignore")) {
		if (parts.length == 2) 
		    ignoreUser(parts[1]);
	    } else if (command.equals("/playlist")) {
		showPlaylist();
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

    protected String handleTabComplete(String[] sentence) {

	String partialName = sentence[sentence.length - 1].toLowerCase() + "(.*)";
	ArrayList<String> users = new ArrayList<String>();
	String replacedSentence = "";

	for (CytubeUser user : userList) {
	    if (user.getUsername().toLowerCase().matches(partialName)) {
		users.add(user.getUsername());
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

    private void handleUserMeta(JSONObject data) throws JSONException {
	for (CytubeUser user : userList) {
	    if (user.getUsername().equalsIgnoreCase(data.getString("name"))) 
		user.setMuted(data.getJSONObject("meta").getBoolean("muted"));
	}

    }

    protected void hideUserlist() {
	userListScrollPane.setVisible(!userListScrollPane.isVisible());
	parent.repaint();
    }

    protected void ignoreUser(String username) {
	for (CytubeUser user : userList) {
	    if (user.getUsername().equalsIgnoreCase(username)) {
		user.setIgnore(!user.isIgnore());
		return;
	    }
	}
    }

    private void moveVideo(JSONObject obj) throws JSONException, BadLocationException {
	int from = obj.getInt("from");
	int after = obj.getInt("after");

	addVideo(null, true, after, deleteVideo(from, true));
	if (getPlaylistFrame() != null) {
	    getPlaylistFrame().setPlaylist(playlist);
	    getPlaylistFrame().drawPlaylist();
	}

    }

    private void NewMessageActionPerformed(ActionEvent evt) {
	this.handleGUICommand(getNewMessageTextField().getText());
	getNewMessageTextField().setText(null);
    }

    private void removeUser(String username) throws BadLocationException {
	SimpleAttributeSet attributes = new SimpleAttributeSet();
	attributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
	getStyledMessagesDocument().insertString(
		getStyledMessagesDocument().getLength(), 
		CytubeUtils.formatMessage("[Client]", username + " left the room", 
			System.currentTimeMillis()), attributes);

	for (CytubeUser user : userList) {
	    if (user.getUsername().equalsIgnoreCase(username)) {
		if (user.isInPrivateMessage())
		    user.getPmFrame().handleUserLeftRoom();
		userList.remove(user);
		break;
	    }
	}
    }

    protected void sendVideo(String id, String type, String pos, boolean temp) throws JSONException {
	JSONObject json = new JSONObject();
	json.putOpt("id", id);
	json.putOpt("type", type);
	json.putOpt("pos", pos);
	json.put("duration", 0);
	json.put("temp", temp);

	chat.getSocket().emit("queue", json);

    }

    private void setAfk(String name, boolean afk) {
	for (CytubeUser user : userList) {
	    if (user.getUsername().equalsIgnoreCase(name)) {
		user.setAfk(afk);
		break;
	    }
	}
    }

    protected void showPlaylist() {
	setPlaylistFrame(new CytubePlaylist(getPlayList(), this));
	getPlaylistFrame().setVisible(true);
	try {
	    getPlaylistFrame().drawPlaylist();
	} catch (BadLocationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
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

    protected void startChat() {
	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		getChat().start();
	    }
	});
    }

    private void updateUserList() throws BadLocationException {
	// Number of users. Note: I'm ignoring anons at this time
	userlistTextPane.setText("");
	styledUserlist.insertString(styledUserlist.getLength(), 
		"Users: " + userList.size() + "\n--------------\n", null);

	// Sort userlist
	Collections.sort(userList, new Comparator<CytubeUser>() {
	    @Override
	    public int compare(CytubeUser user1, CytubeUser user2) {
		return user1.getUsername().compareToIgnoreCase(user2.getUsername());
	    }
	});

	// Print the userlist
	for (CytubeUser user : userList) {
	    styledUserlist.insertString(styledUserlist.getLength(), user.getUsername() 
		    + "\n", user.getUserlistStyle());
	}
    }

    protected void privateMessage(String to, String message) throws JSONException {
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
		chatMsg(obj);
	    } else if (event.equals("addUser")) {
		boolean afk = (boolean) obj.getJSONObject("meta").get("afk");
		String username = obj.getString("name") ;
		int rank = (int) obj.get("rank");
		CytubeUser user = new CytubeUser(afk, username, rank, this, false);
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
	    } else if (event.equals("setAFK")) {
		setAfk(obj.getString("name"), (boolean) obj.get("afk"));
		updateUserList();
	    } else if (event.equals("login")) {
		if ((boolean) obj.get("success")) {
		    System.out.println("Logged in");
		    setUsername(obj.getString("name"));
		    user.setUsername(obj.getString("name"));
		} else {
		    JOptionPane.showMessageDialog(null, obj.get("error"));
		    setUsername(null);
		}
	    }  else if (event.equals("setUserMeta")) {
		handleUserMeta(obj);
		updateUserList();
	    } else if (event.equals("queue")) {
		addVideo(obj, false, 0, null);
	    } else if (event.equals("delete")) {
		deleteVideo(obj.getInt("uid"), false);
	    } else if (event.equals("moveVideo")) {
		moveVideo(obj);
	    }
	} catch (JSONException ex) {
	    ex.printStackTrace();
	} catch (BadLocationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
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
		CytubeUser user = new CytubeUser(afk, username, rank, this, false);
		try {
		    addUser(user, false);
		} catch (BadLocationException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	    try {
		updateUserList();
	    } catch (BadLocationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} else if (event.equals("playlist")) {
	    JSONArray videoArray =  data.getJSONArray(0);

	    if (videoArray.length() == 0) {
		getPlaylistFrame().clearPlaylist();
	    }

	    for (int i = 0; i < videoArray.length(); i++) {
		playlist.add(new CytubeVideo(videoArray.getJSONObject(i)));
	    }
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
	for (CytubeUser user : userList) {
	    if (user.getUsername().equalsIgnoreCase(obj.getString("username"))
		    && user.isIgnore()) {
		return;
	    }
	}

	String message = 
		CytubeUtils.formatMessage(obj.getString("username"), 
			obj.getString("msg"), (long) obj.get("time"));

	for (CytubeUser user : userList) {
	    try {
		if (user.getUsername().equals(obj.getString("username")) &&
			!username.equals(obj.getString("username"))) {
		    if (!user.isInPrivateMessage()) {
			user.startPM(message);
			break;
		    } else {
			user.getPmFrame().addMessage(message);
			break;
		    }
		} else if (user.getUsername().equals(obj.getString("to")) && 
			username.equals(obj.getString("username"))) {
		    if (!user.isInPrivateMessage()) {
			user.startPM(message);
			break;
		    } else {
			user.getPmFrame().addMessage(message);
			break;
		    }
		}
	    } catch (BadLocationException e) {
		e.printStackTrace();
		user.getPmFrame().setVisible(false);
		return;
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
    public void onDisconnect() {
	setUsername(null);
	JOptionPane.showMessageDialog(null, "Disconnected");
    }

    @Override
    public void onConnectFailure() {
	JOptionPane.showMessageDialog(null, "Could not connect");
    }

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

    public CytubePlaylist getPlaylistFrame() {
	return playlistFrame;
    }

    public void setPlaylistFrame(CytubePlaylist playlistFrame) {
	this.playlistFrame = playlistFrame;
    }

    public LinkedList<String> getMessageBuffer() {
	return messageBuffer;
    }

    public void setMessageBuffer(LinkedList<String> messageBuffer) {
	this.messageBuffer = messageBuffer;
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

    public LinkedList<CytubeVideo> getPlayList() {
	return playlist;
    }

    public void setPlayList(LinkedList<CytubeVideo> playList) {
	this.playlist = playList;
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

    public String getServer() {
	return server;
    }

    public void setServer(String server) {
	this.server = server;
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
	result = prime * result + ((server == null) ? 0 : server.hashCode());
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
	if (server == null) {
	    if (other.server != null)
		return false;
	} else if (!server.equals(other.server))
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
		+ ", username=" + username + ", messageBuffer=" + getMessageBuffer()
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
