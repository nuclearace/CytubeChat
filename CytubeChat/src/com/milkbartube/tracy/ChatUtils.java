package com.milkbartube.tracy;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.commons.lang3.StringEscapeUtils;

public class ChatUtils {

    private StyledDocument document;
    private CytubeRoom room;
    private PrivateMessageFrame pm;

    public ChatUtils(CytubeRoom room) {
	document = room.getStyledMessagesDocument();
	this.setRoom(room);
    }

    public ChatUtils(CytubeRoom room, PrivateMessageFrame pm) {
	this.room = pm.getRoom();
	document = pm.getPrivateMessageStyledDocument();
    }

    protected void addMessageWithLinks(ArrayList<String> list, boolean pm) 
	    throws BadLocationException {
	list.remove("\n");

	Color color = new Color(0x351FFF);
	StyleContext sc = StyleContext.getDefaultStyleContext();
	AttributeSet attributes = sc.addAttribute(SimpleAttributeSet.EMPTY, 
		StyleConstants.Foreground, color);

	for (String word : list) {
	    if (!word.matches("(.*)(http(s?):/)(/[^/]+).*")) {
		getDocument().insertString(getDocument().
			getLength(), word + " ", null);
	    } else {
		getDocument().insertString(getDocument().
			getLength(), word + " ", attributes);
	    }
	}
	getDocument().insertString(getDocument().
		getLength(), "\n", null);

	if (!pm)
	    room.getMessageBuffer().add("");

	if (!pm && room.getMessageBuffer().size() > 100 && room.getFrameParent().isLimitChatBuffer()) {
	    room.getMessageBuffer().remove();
	    room.getMessagesTextPane().setText(room.getMessagesTextPane().getText()
		    .substring(room.getMessagesTextPane().getText().indexOf('\n')+1));
	}

	if (!room.isStopMessagesAreaScrolling())
	    room.getMessagesTextPane().setCaretPosition(getDocument().getLength());
    }

    protected String formatMessage(String username, String message, long time) {
	String imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
	String htmlTagRegex = "</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>";

	String cleanedString = StringEscapeUtils.unescapeHtml4(message);
	cleanedString = cleanedString.replaceAll(imgRegex, "$1 ");
	cleanedString = cleanedString.replaceAll(htmlTagRegex, "");

	// Add the timestamp
	Date date = new Date(time);
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
	formatter.setTimeZone(TimeZone.getDefault());
	String formattedTime = formatter.format(date);

	return "[" + formattedTime + "] " + username + ": " + cleanedString + " \n";
    }

    public StyledDocument getDocument() {
	return document;
    }

    public void setDocument(StyledDocument document) {
	this.document = document;
    }

    public PrivateMessageFrame getPm() {
	return pm;
    }

    public void setPm(PrivateMessageFrame pm) {
	this.pm = pm;
    }

    public CytubeRoom getRoom() {
	return room;
    }

    public void setRoom(CytubeRoom room) {
	this.room = room;
    }

}
