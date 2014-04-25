package com.milkbartube.tracy;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

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

    protected void chatMsg(JSONObject obj) throws JSONException, BadLocationException {
	ArrayList<String> list = new ArrayList<String>();
	Pattern linkPattern = Pattern.compile("(\\w+:\\/\\/(?:[^:\\/\\[\\]\\s]+|\\[[0-9a-f:]+\\])(?::\\d+)?(?:\\/[^\\/\\s]*)*)");

	String cleanedString = formatMessage(obj.getString("username"), 
		obj.getString("msg"), (long) obj.get("time"));

	Matcher matcher = linkPattern.matcher(cleanedString);

	if (matcher.find()) {
	    for (String word: cleanedString.split(" ")) {
		list.add(word);
	    }
	    addMessageWithLinks(list, 
		    false);

	    if (room.getFrameParent().getClip() != null && room.getFrameParent().isWindowFocus() 
		    && !room.getFrameParent().isUserMuteBoop()
		    || room.getUsername() != null && cleanedString.toLowerCase()
		    .contains(room.getUsername().toLowerCase())) {
		room.getFrameParent().playSound();
	    }
	    return;
	}

	cleanedString = 
		formatMessage(obj.getString("username"), 
			obj.getString("msg"), (long) obj.get("time"));

	if (room.getMessageBuffer().size() > 100 && room.getFrameParent().isLimitChatBuffer()) {
	    room.getMessageBuffer().remove();
	    room.getMessagesTextPane().setText(room.getMessagesTextPane().getText()
		    .substring(room.getMessagesTextPane().getText().indexOf('\n')+1));
	}

	room.getMessageBuffer().add(cleanedString);
	getDocument().insertString(getDocument().
		getLength(), room.getMessageBuffer().peekLast(), null);

	if (!room.getFrameParent().isLimitChatBuffer())
	    room.getMessagesTextPane().setCaretPosition(getDocument().getLength());

	if (room.getFrameParent().getClip() != null && room.getFrameParent().isWindowFocus() 
		&& !room.getFrameParent().isUserMuteBoop()
		|| room.getUsername() != null && cleanedString.toLowerCase()
		.contains(room.getUsername().toLowerCase())) {
	    room.getFrameParent().playSound();
	}
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
