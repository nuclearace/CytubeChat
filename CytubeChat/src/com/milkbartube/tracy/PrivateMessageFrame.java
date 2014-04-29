package com.milkbartube.tracy;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.json.JSONException;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;

public class PrivateMessageFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField newPrivateMessageTextField;
    private JScrollPane privateMessageScrollPane;
    private JTextPane privateMessageTextPane;
    private StyledDocument privateMessageStyledDocument;

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
	setTitle(user.getUsername() + " (" + room.getRoom() + ")");
    }

    /**
     * Create the frame.
     */
    private void buildPrivateMessageFrame() {
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

	privateMessageTextPane = new JTextPane();
	privateMessageTextPane.setEditable(false);
	privateMessageScrollPane.setViewportView(privateMessageTextPane);
	privateMessageTextPane.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		int pos = privateMessageTextPane.viewToModel(e.getPoint());
		Element element = getPrivateMessageStyledDocument().getCharacterElement(pos);

		AttributeSet as = element.getAttributes();
		if (StyleConstants.getForeground(as).equals(new Color(0x351FFF))) {
		    try {
			room.handleLink(getPrivateMessageStyledDocument().getText(element.getStartOffset(), 
				((element.getEndOffset() - element.getStartOffset()) - 1)));
		    } catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		    }
		}
	    }
	});
	setPrivateMessageStyledDocument(privateMessageTextPane.getStyledDocument());
	contentPane.setLayout(gl_contentPane);
    }

    private void newMessageActionPerformed() {
	try {
	    String text = getNewMessageTextField().getText();
	    room.privateMessage(user.getUsername(), 
		    text);
	    getNewMessageTextField().setText("");
	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void addMessage(String message) throws BadLocationException {
	ArrayList<String> list = new ArrayList<String>();
	Pattern linkPattern = 
		Pattern.compile("(\\w+:\\/\\/(?:[^:\\/\\[\\]\\s]+|\\[[0-9a-f:]+\\])(?::\\d+)?(?:\\/[^\\/\\s]*)*)");

	Matcher matcher = linkPattern.matcher(message);

	if (matcher.find()) {
	    for (String string: message.split(" ")) {
		list.add(string);
	    }

	    try {
		CytubeUtils.addMessageWithLinks(list, true, getPrivateMessageStyledDocument(), getRoom());
	    } catch (BadLocationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    return;
	}


	getPrivateMessageStyledDocument().insertString(getPrivateMessageStyledDocument().
		getLength(), message, null);

	if (!this.isFocused())
	    room.getFrameParent().playSound();

	privateMessageTextPane.setCaretPosition(
		privateMessageTextPane.getDocument().getLength());
    }

    protected void handleTabComplete() {
	String[] sentence = newPrivateMessageTextField.getText().toString().split(" ");
	newPrivateMessageTextField.setText(room.handleTabComplete(sentence));
    }

    protected void handleUserLeftRoom() throws BadLocationException {
	getPrivateMessageStyledDocument().insertString(getPrivateMessageStyledDocument().
		getLength(),"\n" + user.getUsername() + " left the room", null);
	newPrivateMessageTextField.setEditable(false);
    }

    public JTextField getNewMessageTextField() {
	return newPrivateMessageTextField;
    }

    public void setNewMessageTextField(JTextField newMessageTextField) {
	this.newPrivateMessageTextField = newMessageTextField;
    }

    public StyledDocument getPrivateMessageStyledDocument() {
	return privateMessageStyledDocument;
    }

    public void setPrivateMessageStyledDocument(
	    StyledDocument privateMessageStyledDocument) {
	this.privateMessageStyledDocument = privateMessageStyledDocument;
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
