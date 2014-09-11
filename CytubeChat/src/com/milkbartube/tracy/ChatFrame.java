package com.milkbartube.tracy;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

public class ChatFrame extends JFrame implements WindowFocusListener {

    private static final long serialVersionUID = -3120953406569989166L;
    private JMenuBar menuBar;
    private JMenu mnMenu;
    private JMenuItem mntmLogin;
    private JMenuItem mntmDisconnect;
    private JMenuItem mntmQuit;
    private JTabbedPane tabbedPane;

    private Clip clip;
    private boolean limitChatBuffer = false;
    private boolean userMuteBoop = true;
    private boolean windowFocus = false;
    private JMenuItem mntmJoinRoom;
    private JMenuItem mntmCloseTab;
    private JMenuItem mntmHideUserlist;
    private JMenuItem mntmPlaylist;
    // End variables

    public ChatFrame() {
	setPreferredSize(new Dimension(700, 500));

	initComponents();
	setVisible(true);
	setLocationRelativeTo(null);
	try {
	    URL soundFile = new URL("http://cytu.be/boop.wav");
	    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);

	    this.setClip(AudioSystem.getClip());
	    getClip().open(audioIn);
	} catch (Exception e) {
	    setClip(null);
	    e.printStackTrace();
	}

	joinRoom();
    }

    private void initComponents() {

	setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

	menuBar = new JMenuBar();
	menuBar.setBackground(Color.WHITE);
	setJMenuBar(menuBar);

	mnMenu = new JMenu("Menu");
	mnMenu.setBackground(Color.WHITE);
	menuBar.add(mnMenu);

	mntmHideUserlist = new JMenuItem("Hide Userlist");
	mntmHideUserlist.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_U, (Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask())));
	mntmHideUserlist.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		CytubeRoom c = (CytubeRoom) tabbedPane.getSelectedComponent();
		c.hideUserlist();
	    }
	});
	
		mntmLogin = new JMenuItem("Login");
		mnMenu.add(mntmLogin);
		mntmLogin.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_L, (Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask())));
		mntmLogin.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			handleLogin();
		    }
		});
	mnMenu.add(mntmHideUserlist);

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
	
	mntmPlaylist = new JMenuItem("Playlist");
	mntmPlaylist.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    CytubeRoom c = (CytubeRoom) tabbedPane.getSelectedComponent();
		    c.showPlaylist();
		}
	});
	mntmPlaylist.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_P, (Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask())));
	mnMenu.add(mntmPlaylist);
	mnMenu.add(mntmJoinRoom);

	mntmCloseTab = new JMenuItem("Close Tab");
	mntmCloseTab.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		CytubeRoom c = (CytubeRoom) tabbedPane.getSelectedComponent();
		try {
		    System.out.println("Closing room " + c.getRoom());
		    c.getSocket().disconnect();
		    c.closePMFrames();
		} catch (Exception e1) {
		    e1.printStackTrace();
		} finally {
		    tabbedPane.remove(c);
		}
	    }
	});
	mntmCloseTab.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_W, (Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask())));
	mnMenu.add(mntmCloseTab);

	mntmDisconnect = new JMenuItem("Disconnect");
	mntmDisconnect.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_D, (Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask())));
	mntmDisconnect.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		CytubeRoom c = (CytubeRoom) tabbedPane.getSelectedComponent();
		c.getSocket().disconnect();
		try {
		    c.getStyledMessagesDocument().insertString(c.getStyledMessagesDocument()
			    .getLength(),
			    "\nDisconnected!\n", null);
		} catch (BadLocationException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}
	    }
	});
	mnMenu.add(mntmDisconnect);

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
	tabbedPane.addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(ChangeEvent e) {
		CytubeRoom c = (CytubeRoom) tabbedPane.getSelectedComponent();
		if (c == null)
		    return;
		setTitle(c.getCurrentMedia());
	    }

	});

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

    public void changeColors(int r1, int g1, int b1, int r2, int g2, int b2) {
	int totalTabs = tabbedPane.getTabCount();
	for(int i = 0; i < totalTabs; i++)
	{
	    CytubeRoom c = (CytubeRoom) tabbedPane.getComponentAt(i);

	    c.getMessagesTextPane().setBackground(new java.awt.Color(r1, g1, b1));
	    c.getMessagesTextPane().setForeground(new java.awt.Color(r2, g2, b2));

	    c.getUserlistTextPane().setBackground(new java.awt.Color(r1, g1, b1));
	    c.getUserlistTextPane().setForeground(new java.awt.Color(r2, g2, b2));

	    c.getNewMessageTextField().setBackground(new java.awt.Color(r1, g1, b1));
	    c.getNewMessageTextField().setForeground(new java.awt.Color(r2, g2, b2));
	}
    }

    public void changeMedia(String media) {
	setTitle("Now Playing: " + media);
    }

    private String getSocketURL(String server) 
	    throws MalformedURLException, IOException {
	String urlString = server;
	Pattern socketPattern = Pattern.compile(".*IO_URL=['? | \"?](.*)['? | \"?],WEB_URL");
	Matcher matcher;

	//TODO handle when user enters the socketURL
	if (!urlString.startsWith("http://")) {
	    urlString = "http://" + urlString + "/sioconfig";
	} else {
	    urlString = urlString + "/sioconfig";
	}

	URL url = new URL(urlString);
	BufferedReader br = null;

	try {
	    URLConnection conn = url.openConnection();

	    br = new BufferedReader(
		    new InputStreamReader(conn.getInputStream()));
	    String inputLine;
	    while ((inputLine = br.readLine()) != null) {
		matcher = socketPattern.matcher(inputLine);
		if (matcher.find()) {
		    return matcher.group(1);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    br.close();
	}
	return "Error";

    }

    public void handleLogin() {
	CytubeRoom panel = 
		(CytubeRoom) tabbedPane.getSelectedComponent();
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
	    panel.getSocket().login(username, password);
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
	if (room == null)
	    return;

	room = roomInput.getRoom().replace(" ", "");
	String roomPassword = roomInput.getPassword();
	String server = roomInput.getServer();

	int totalTabs = tabbedPane.getTabCount();

	if (!room.isEmpty()) {
	    try {
		server = getSocketURL(server);
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		JOptionPane.showMessageDialog(null, "Something is wrong with "
			+ "your URL");
		return;
	    }
	}
	System.out.println(server);
	if (server.startsWith("http://")) {
	    CytubeRoom panel = new CytubeRoom(room, roomPassword, this, server);
	    for (int i = 0; i < totalTabs; i++) {
		CytubeRoom c = (CytubeRoom) tabbedPane.getComponentAt(i);
		if (c.equals(panel))
		    return;
	    }
	    tabbedPane.addTab(room + " (" +server.replaceAll("http\\:\\/\\/(.*\\.)?(.*\\..*)\\:.*", "$2")
		    + ")", panel);
	    getTabbedPane().setSelectedComponent(panel);
	    panel.startChat();
	} else {
	    JOptionPane.showMessageDialog(null, "Something has gone wrong fetching the socketURL");
	}
    }

    public void playSound() {
	getClip().start();
	getClip().setFramePosition(0);
    }

    public boolean isWindowFocus() {
	return windowFocus;
    }

    public void setWindowFocus(boolean windowFocus) {
	this.windowFocus = windowFocus;
    }

    public boolean isUserMuteBoop() {
	return userMuteBoop; 
    }

    public void setUserMuteBoop(boolean userMuteBoop) {
	this.userMuteBoop = userMuteBoop;
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
