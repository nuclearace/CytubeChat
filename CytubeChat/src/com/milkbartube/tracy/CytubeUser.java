package com.milkbartube.tracy;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class CytubeUser {

    private boolean afk;
    private boolean ignore;
    private boolean inPrivateMessage;
    private boolean muted;
    private String username;
    private int rank;
    private CytubeRoom room;
    private PrivateMessageFrame pmFrame;

    public CytubeUser(boolean afk, String name, int rank, CytubeRoom room, boolean ignore) {
	this.afk = afk;
	this.username = name;
	this.rank = rank;
	this.room = room;
	this.ignore = ignore;
    }

    private AttributeSet addAfkStyle(AttributeSet attributes) {
	StyleContext sc = StyleContext.getDefaultStyleContext();
	if (getAfk()) {
	    attributes = 
		    sc.addAttribute(attributes, StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
	}
	return attributes;
    }

    private AttributeSet addMutedStyle(AttributeSet attributes) {
	StyleContext sc = StyleContext.getDefaultStyleContext();
	if (isMuted()) {
	    attributes = 
		    sc.addAttribute(attributes, StyleConstants.CharacterConstants.StrikeThrough, Boolean.TRUE);
	}
	return attributes;
    }

    private AttributeSet addRankStyle() {
	StyleContext sc = StyleContext.getDefaultStyleContext();

	switch (getRank()) {
	case 0:
	    AttributeSet attributes = sc.addAttribute(SimpleAttributeSet.EMPTY, 
		    StyleConstants.Foreground, new Color(0x969696));
	    return attributes;
	case 2:
	    AttributeSet attributes2 = sc.addAttribute(SimpleAttributeSet.EMPTY, 
		    StyleConstants.Foreground, new Color(0x13BF0D));

	    return attributes2;

	case 3:
	    AttributeSet attributes3 = sc.addAttribute(SimpleAttributeSet.EMPTY, 
		    StyleConstants.Foreground, new Color(0xF0B22E));

	    return attributes3;

	case 4:
	    AttributeSet attributes4 = sc.addAttribute(SimpleAttributeSet.EMPTY, 
		    StyleConstants.Foreground, new Color(0x5C00FA));

	    return attributes4;

	case 5:
	    AttributeSet attributes5 = sc.addAttribute(SimpleAttributeSet.EMPTY, 
		    StyleConstants.Foreground, new Color(0xFA00BB));

	    return attributes5;

	default:
	    AttributeSet attributes6 = sc.addAttribute(
		    SimpleAttributeSet.EMPTY, StyleConstants.CharacterConstants.Bold, Boolean.FALSE);

	    if (getRank() >= 255) {
		attributes6 = 
			sc.addAttribute(attributes6, StyleConstants.Foreground, new Color(0xFA0000));
	    }
	    return attributes6;
	}
    }

    protected AttributeSet getUserlistStyle() {
	return addAfkStyle(addMutedStyle(addRankStyle()));
    }

    protected void startPM(ArrayList<String> message) throws BadLocationException {
	setPmFrame(new PrivateMessageFrame(getRoom(), this));
	getPmFrame().addMessage(message);
	setInPrivateMessage(true);
	getPmFrame().setVisible(true);
    }

    public boolean getAfk() {
	return afk;
    }

    public void setAfk(boolean afk) {
	this.afk = afk;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String name) {
	this.username = name;
    }
    public boolean isIgnore() {
	return ignore;
    }

    public void setIgnore(boolean ignore) {
	this.ignore = ignore;
    }

    public boolean isMuted() {
	return muted;
    }

    public void setMuted(boolean muted) {
	this.muted = muted;
    }

    public boolean isInPrivateMessage() {
	return inPrivateMessage;
    }

    public void setInPrivateMessage(boolean inPrivateMessage) {
	this.inPrivateMessage = inPrivateMessage;
    }

    public PrivateMessageFrame getPmFrame() {
	return pmFrame;
    }

    public void setPmFrame(PrivateMessageFrame pmFrame) {
	this.pmFrame = pmFrame;
    }

    public int getRank() {
	return rank;
    }

    public void setRank(int rank) {
	this.rank = rank;
    }

    public CytubeRoom getRoom() {
	return room;
    }

    public void setRoom(CytubeRoom room) {
	this.room = room;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((username == null) ? 0 : username.hashCode());
	result = prime * result + rank;
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
	CytubeUser other = (CytubeUser) obj;
	if (username == null) {
	    if (other.username != null)
		return false;
	} else if (!username.equals(other.username))
	    return false;
	if (rank != other.rank)
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "CyTubeUser [afk=" + afk + ", name=" + username + ", rank=" + rank
		+ "]";
    }
}
