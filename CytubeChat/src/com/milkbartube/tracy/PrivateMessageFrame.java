package com.milkbartube.tracy;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.json.JSONException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PrivateMessageFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField newPrivateMessageTextField;
    private JScrollPane privateMessageScrollPane;
    private JTextArea privateMessageTextArea;

    private CytubeRoom room;
    private CytubeUser user;

    public PrivateMessageFrame(CytubeRoom room, final CytubeUser user) {
	addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosed(WindowEvent e) {
		user.setInPrivateMessage(false);
	    }
	});
	this.room = room;
	this.user = user;
	buildPrivateMessageFrame();
	setTitle(user.getName() + " (" + room.getRoom() + ")");
    }

    /**
     * Create the frame.
     */
    public void buildPrivateMessageFrame() {
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	setBounds(100, 100, 450, 300);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	setContentPane(contentPane);

	privateMessageScrollPane = new JScrollPane();
	privateMessageScrollPane.setAutoscrolls(true);

	newPrivateMessageTextField = new JTextField();
	newPrivateMessageTextField.setFocusTraversalKeysEnabled(false);
	newPrivateMessageTextField.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		newMessageActionPerformed();
	    }
	});
	newPrivateMessageTextField.addKeyListener(new KeyListener() {
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
	newPrivateMessageTextField.setColumns(10);
	GroupLayout gl_contentPane = new GroupLayout(contentPane);
	gl_contentPane.setHorizontalGroup(
		gl_contentPane.createParallelGroup(Alignment.LEADING)
		.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
			.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addComponent(newPrivateMessageTextField, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
				.addComponent(privateMessageScrollPane, GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
				.addGap(0))
		);
	gl_contentPane.setVerticalGroup(
		gl_contentPane.createParallelGroup(Alignment.LEADING)
		.addGroup(gl_contentPane.createSequentialGroup()
			.addComponent(privateMessageScrollPane, GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(newPrivateMessageTextField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
			.addContainerGap())
		);

	privateMessageTextArea = new JTextArea();
	privateMessageTextArea.setEditable(false);
	privateMessageScrollPane.setViewportView(privateMessageTextArea);
	contentPane.setLayout(gl_contentPane);
    }

    protected void newMessageActionPerformed() {
	try {
	    String text = getNewMessageTextField().getText();
	    room.privateMessage(user.getName(), 
		    text);
	    getNewMessageTextField().setText("");
	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void addMessage(String message) {
	if (!privateMessageTextArea.getText().equals(""))
	    privateMessageTextArea.append("\n" + message);
	else
	    privateMessageTextArea.append(message);

	if (!this.isFocused())
	    room.getFrameParent().playSound();

	privateMessageTextArea.setCaretPosition(
		privateMessageTextArea.getDocument().getLength());
    }

    protected void handleTabComplete() {
	String[] sentence = newPrivateMessageTextField.getText().toString().split(" ");
	newPrivateMessageTextField.setText(room.handleTabComplete(sentence));
    }

    protected void handleUserLeftRoom() {
	privateMessageTextArea.append("\n" + user.getName() + " left the room");
	newPrivateMessageTextField.setEditable(false);
    }

    public JTextField getNewMessageTextField() {
	return newPrivateMessageTextField;
    }

    public void setNewMessageTextField(JTextField newMessageTextField) {
	this.newPrivateMessageTextField = newMessageTextField;
    }

    public CytubeRoom getRoom() {
	return room;
    }

    public void setRoom(CytubeRoom room) {
	this.room = room;
    }

    public CytubeUser getUser() {
	return user;
    }

    public void setUser(CytubeUser user) {
	this.user = user;
    }
}
