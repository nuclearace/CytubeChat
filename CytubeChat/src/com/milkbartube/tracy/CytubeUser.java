package com.milkbartube.tracy;

import javax.swing.text.BadLocationException;

public class CytubeUser {

    private boolean afk;
    private boolean ignore;
    private boolean inPrivateMessage;
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

    protected void startPM(String message) throws BadLocationException {
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
