package com.github.zjzcn.ceper.monitor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Metric implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private String value;
	
	private long timestamp;
	
	private Map<String, Object> tags = new HashMap<String, Object>();

	public Metric(String name, String value) {
		this.name = name;
		this.value = value;
		this.timestamp = System.currentTimeMillis();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Object> getTags() {
		return tags;
	}

	public void setTags(Map<String, Object> tags) {
		this.tags = tags;
	}
	
	public void addTag(String key, Object value) {
		this.tags.put(key, value);
	}
	
}
