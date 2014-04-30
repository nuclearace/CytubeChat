package com.milkbartube.tracy;

import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

public class CytubeUtils {

    protected static void addMessageWithLinks(ArrayList<String> list, boolean pm, StyledDocument doc, CytubeRoom room) 
	    throws BadLocationException {
	list.remove("\n");

	StyleContext sc = StyleContext.getDefaultStyleContext();
	AttributeSet attributes = sc.addAttribute(SimpleAttributeSet.EMPTY, 
		StyleConstants.Foreground, new Color(0x351FFF));

	for (String word : list) {
	    if (!word.matches("(.*)(http(s?):/)(/[^/]+).*")) {
		doc.insertString(doc.getLength(), word + " ", null);
	    } else {
		doc.insertString(doc.getLength(), word + " ", attributes);
	    }
	}
	doc.insertString(doc.getLength(), "\n", null);

	if (!pm)
	    room.getMessageBuffer().add("");

	if (!pm && room.getMessageBuffer().size() > 100 && room.getFrameParent().isLimitChatBuffer()) {
	    room.getMessageBuffer().remove();
	    room.getMessagesTextPane().setText(room.getMessagesTextPane().getText()
		    .substring(room.getMessagesTextPane().getText().indexOf('\n')+1));
	}

	if (!room.isStopMessagesAreaScrolling())
	    room.getMessagesTextPane().setCaretPosition(doc.getLength());
    }

    protected static String formatMessage(String username, String message, long time) {
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

    protected static void handleLink(String uri) {
	try {
	    Desktop.getDesktop().browse(new URI(uri));
	} catch (IOException | URISyntaxException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
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
}
