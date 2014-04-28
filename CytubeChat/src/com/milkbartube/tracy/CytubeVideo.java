package com.milkbartube.tracy;

import org.json.JSONException;
import org.json.JSONObject;

public class CytubeVideo {

    private String id;
    private String title;
    private long seconds;
    private String duration;
    private String type;
    private int uid;
    private boolean temp;
    private String queueBy;

    public CytubeVideo(JSONObject json) throws JSONException {
	JSONObject media = json.getJSONObject("media");
	setId(media.getString("id"));
	setTitle(media.getString("title"));
	setSeconds(media.getLong("seconds"));
	setDuration(media.getString("duration"));
	setType(media.getString("type"));
	
	setUid(json.getInt("uid"));
	setTemp(json.getBoolean("temp"));
	setQueueBy(json.getString("queueby"));
    }


    public String getId() {
	return id;
    }
    public void setId(String id) {
	this.id = id;
    }
    public String getTitle() {
	return title;
    }
    public void setTitle(String title) {
	this.title = title;
    }
    public long getSeconds() {
	return seconds;
    }
    public void setSeconds(long seconds) {
	this.seconds = seconds;
    }
    public String getDuration() {
	return duration;
    }
    public void setDuration(String duration) {
	this.duration = duration;
    }
    public String getType() {
	return type;
    }
    public void setType(String type) {
	this.type = type;
    }
    public int getUid() {
	return uid;
    }
    public void setUid(int uid) {
	this.uid = uid;
    }
    public boolean isTemp() {
	return temp;
    }
    public void setTemp(boolean temp) {
	this.temp = temp;
    }
    public String getQueueBy() {
	return queueBy;
    }
    public void setQueueBy(String queueBy) {
	this.queueBy = queueBy;
    }


    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((duration == null) ? 0 : duration.hashCode());
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	result = prime * result + ((queueBy == null) ? 0 : queueBy.hashCode());
	result = prime * result + (int) (seconds ^ (seconds >>> 32));
	result = prime * result + (temp ? 1231 : 1237);
	result = prime * result + ((title == null) ? 0 : title.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	result = prime * result + uid;
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
	CytubeVideo other = (CytubeVideo) obj;
	if (duration == null) {
	    if (other.duration != null)
		return false;
	} else if (!duration.equals(other.duration))
	    return false;
	if (id == null) {
	    if (other.id != null)
		return false;
	} else if (!id.equals(other.id))
	    return false;
	if (queueBy == null) {
	    if (other.queueBy != null)
		return false;
	} else if (!queueBy.equals(other.queueBy))
	    return false;
	if (seconds != other.seconds)
	    return false;
	if (temp != other.temp)
	    return false;
	if (title == null) {
	    if (other.title != null)
		return false;
	} else if (!title.equals(other.title))
	    return false;
	if (type == null) {
	    if (other.type != null)
		return false;
	} else if (!type.equals(other.type))
	    return false;
	if (uid != other.uid)
	    return false;
	return true;
    }


    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("CytubeVideo [id=");
	builder.append(id);
	builder.append(", title=");
	builder.append(title);
	builder.append(", seconds=");
	builder.append(seconds);
	builder.append(", duration=");
	builder.append(duration);
	builder.append(", type=");
	builder.append(type);
	builder.append(", uid=");
	builder.append(uid);
	builder.append(", temp=");
	builder.append(temp);
	builder.append(", queueBy=");
	builder.append(queueBy);
	builder.append("]");
	return builder.toString();
    }

}
