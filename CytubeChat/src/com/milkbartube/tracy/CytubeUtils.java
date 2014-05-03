package com.milkbartube.tracy;

import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class CytubeUtils {

    protected static void addMessageWithLinks(ArrayList<String> messageList, boolean pm, 
	    StyledDocument doc, CytubeRoom room) 
		    throws BadLocationException {

	ArrayList<String> message = new ArrayList<String>();
	StyleContext sc = StyleContext.getDefaultStyleContext();
	AttributeSet attributes = sc.addAttribute(SimpleAttributeSet.EMPTY, 
		StyleConstants.Foreground, new Color(0x351FFF));

	SimpleAttributeSet attributes2 = new SimpleAttributeSet();
	attributes2.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);

	for (String word : messageList.get(2).split(" ")) {
	    message.add(word);
	}

	for (int i = 0; i < messageList.size(); i++) {
	    if (i == 0) {
		doc.insertString(doc.getLength(), messageList.get(i), null);
	    } else if (i == 1) {
		doc.insertString(doc.getLength(), messageList.get(i), attributes2);
	    } else if (i == 2) {
		for (String word : message) {
		    if (!word.matches("(.*)(http(s?):/)(/[^/]+).*")) {
			doc.insertString(doc.getLength(), word + " ", null);
		    } else if (word.matches("(.*)(http(s?):/)(/[^/]+).*")) {
			doc.insertString(doc.getLength(), word + " ", attributes);
		    } 
		}
	    }
	}
	doc.insertString(doc.getLength(), "\n", null);

	if (!pm && room.getMessageBuffer().size() > 100 && room.getFrameParent().isLimitChatBuffer()) 
	    doc.remove(0, room.getMessageBuffer().remove().length());

	if (!room.isStopMessagesAreaScrolling())
	    room.getMessagesTextPane().setCaretPosition(doc.getLength());
    }

    protected static ArrayList<String> formatMessage(String username, String message, long time) {
	String imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
	String htmlTagRegex = "</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>";
	ArrayList<String> messageArray = new ArrayList<String>();

	String cleanedString = StringEscapeUtils.unescapeHtml4(message);
	cleanedString = cleanedString.replaceAll(imgRegex, "$1 ");
	cleanedString = cleanedString.replaceAll(htmlTagRegex, "");

	// Add the timestamp
	Date date = new Date(time);
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
	formatter.setTimeZone(TimeZone.getDefault());
	String formattedTime = formatter.format(date);

	messageArray.add("[" + formattedTime + "] ");
	messageArray.add(username + ": ");
	messageArray.add(cleanedString);

	return  messageArray;
    }

    protected static void handleLink(String uri) {
	uri.replaceAll("\n", "");
	try {
	    Desktop.getDesktop().browse(new URI(uri));
	} catch (IOException | URISyntaxException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    protected static String handleTabComplete(String[] sentence, ArrayList<CytubeUser> userlist) {

	String partialName = sentence[sentence.length - 1].toLowerCase() + "(.*)";
	ArrayList<String> users = new ArrayList<String>();
	String replacedSentence = "";

	for (CytubeUser user : userlist) {
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
	    sentence[sentence.length - 1] = smallestComplete(users);
	    for (String word : sentence) {
		replacedSentence += word + " ";
	    }
	    replacedSentence = 
		    replacedSentence.substring(0, replacedSentence.length() - 1);
	    return replacedSentence;
	}
    }

    protected static String idToURL(String id, String type) 
	    throws BadLocationException, MalformedURLException {
	String url;

	switch (type) {
	case "yt":
	    url = "http://youtube.com/watch?v=" + id;
	    break;
	case "vi":
	    url = "http://vimeo.com/" + id;
	    break;
	case "dm":
	    url = "http://dailymotion.com/video/" + id;
	    break;
	case "sc":
	    url = id;
	    break;
	case "li":
	    url = "http://livestream.com/" + id;
	    break;
	case "tw":
	    url = "http://twitch.tv/" + id;
	    break;
	case "jt":
	    url = "http://justin.tv/" + id;
	    break;
	case "rt":
	    url = id;
	    break;
	case "jw":
	    url = id;
	    break;
	case "im":
	    url = "http://imgur.com/a/" + id;
	    break;
	case "us":
	    url = "http://ustream.tv/" + id;
	    break;
	case "gd":
	    url = id;
	    break;
	default:
	    url = id;
	}
	return url;
    }

    protected static String[] parseVideoUrl(String url) {
	// This is really ugly
	Pattern youtubePattern1 = Pattern.compile("youtube\\.com\\/watch\\?v=([^&#]+)");
	Pattern youtubePattern2 = Pattern.compile("youtu\\.be\\/([^&#]+)");
	Pattern youtubePlaylist = Pattern.compile("youtube\\.com\\/playlist\\?list=([^&#]+)");
	Pattern twitchPattern = Pattern.compile("twitch\\.tv\\/([^&#]+)");
	Pattern justintvPattern = Pattern.compile("justin\\.tv\\/([^&#]+)");
	Pattern livestreamPattern = Pattern.compile("livestream\\.com\\/([^&#]+)");
	Pattern ustreamPattern = Pattern.compile("ustream\\.tv\\/([^&#]+)");
	Pattern vimeoPattern = Pattern.compile("vimeo\\.com\\/([^&#]+)");
	Pattern dailymotionPattern = Pattern.compile("dailymotion\\.com\\/video\\/([^&#]+)");
	Pattern soundcloudPattern = Pattern.compile("soundcloud\\.com\\/([^&#]+)");
	Pattern googlePattern = Pattern.compile("docs\\.google\\.com\\/file\\/d\\/(.*?)\\/edit");

	Matcher matcher1 = youtubePattern1.matcher(url);
	Matcher matcher2 = youtubePattern2.matcher(url);
	Matcher matcher3 = youtubePlaylist.matcher(url);
	Matcher matcher4 = twitchPattern.matcher(url);
	Matcher matcher5 = justintvPattern.matcher(url);
	Matcher matcher6 = livestreamPattern.matcher(url);
	Matcher matcher7 = ustreamPattern.matcher(url);
	Matcher matcher8 = vimeoPattern.matcher(url);
	Matcher matcher9 = dailymotionPattern.matcher(url);
	Matcher matcher10 = soundcloudPattern.matcher(url);
	Matcher matcher11 = googlePattern.matcher(url);

	if (matcher1.find()) {
	    return new String[]{matcher1.group(1), "yt"};
	}

	if (matcher2.find()) {
	    return new String[]{matcher2.group(1), "yt"};
	}

	if (matcher3.find()) {
	    return new String[]{matcher3.group(1), "yp"};
	}

	if (matcher4.find()) {
	    return new String[]{matcher4.group(1), "tw"};
	}

	if (matcher5.find()) {
	    return new String[]{matcher5.group(1), "jt"};
	}

	if (matcher6.find()) {
	    return new String[]{matcher6.group(1), "li"};
	}

	if (matcher7.find()) {
	    return new String[]{matcher7.group(1), "us"};
	}

	if (matcher8.find()) {
	    return new String[]{matcher8.group(1), "vm"};
	}

	if (matcher9.find()) {
	    return new String[]{matcher9.group(1), "dm"};
	}

	if (matcher10.find()) {
	    return new String[]{url, "sc"};
	}

	if (matcher11.find()) {
	    return new String[]{matcher11.group(1), "gd"};
	}

	return null;
    }

    private static String smallestComplete(ArrayList<String> users) {
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
}
