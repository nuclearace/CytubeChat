package com.milkbartube.tracy;

public class CytubeUser {
    
    private boolean afk;
    private String name;
    private int rank;
   
    public CytubeUser(boolean afk, String name, int rank) {
	super();
	this.afk = afk;
	this.name = name;
	this.rank = rank;
    }
    
    /**
     * @return the afk
     */
    public boolean getAfk() {
        return afk;
    }
    /**
     * @param afk the afk to set
     */
    public void setAfk(boolean afk) {
        this.afk = afk;
    }
    /**
     * @return the name
     */
    public String getName() {
	return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
	this.name = name;
    }
    /**
     * @return the rank
     */
    public int getRank() {
	return rank;
    }
    /**
     * @param rank the rank to set
     */
    public void setRank(byte rank) {
	this.rank = rank;
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
