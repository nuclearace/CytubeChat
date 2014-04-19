package com.milkbartube.tracy;

public class CytubeUser {
    
    private boolean afk;
    private boolean inPrivateMessage;
    private String name;
    private int rank;
    private CytubeRoom room;
    private PrivateMessageFrame pmFrame;
   
    public CytubeUser(boolean afk, String name, int rank, CytubeRoom room) {
	this.afk = afk;
	this.name = name;
	this.rank = rank;
	this.room = room;
    }
    
    public void startPM(String message) {
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

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
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

    public void setRank(byte rank) {
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
	result = prime * result + ((name == null) ? 0 : name.hashCode());
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
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	if (rank != other.rank)
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "CyTubeUser [afk=" + afk + ", name=" + name + ", rank=" + rank
		+ "]";
    }
}
