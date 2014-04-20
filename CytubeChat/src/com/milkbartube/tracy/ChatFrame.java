package com.milkbartube.tracy;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	    this.setClip(null);
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
	mnMenu.add(mntmJoinRoom);

	mntmCloseTab = new JMenuItem("Close Tab");
	mntmCloseTab.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		CytubeRoom c = (CytubeRoom) tabbedPane.getSelectedComponent();
		try {
		    System.out.println("Closing room " + c.getRoom());
		    c.getChat().disconnectChat();
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
		c.getChat().disconnectChat();
		c.getMessagesTextArea().append("\nDisconnected!\n");
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
	    panel.getChat().login(username, password);
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

	String room = roomInput.getRoom().replace(" ", "");
	if (room == null)
	    return;
	String roomPassword = roomInput.getPassword();

	int totalTabs = tabbedPane.getTabCount();
	for(int i = 0; i < totalTabs; i++) {
	    CytubeRoom c = (CytubeRoom) tabbedPane.getComponentAt(i);
	    if (c.getRoom().toLowerCase().equals(room.toLowerCase()))
		return;
	}

	if (!room.isEmpty()) {
	    CytubeRoom panel = new CytubeRoom(room, roomPassword, this); 
	    tabbedPane.addTab(room, panel);
	    getTabbedPane().setSelectedComponent(panel);
	}
    }

    public void playSound() {
	this.getClip().start();
	this.getClip().setFramePosition(0);
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
