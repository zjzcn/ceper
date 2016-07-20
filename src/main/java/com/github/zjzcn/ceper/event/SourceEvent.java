package com.github.zjzcn.ceper.event;

import java.util.HashMap;
import java.util.Map;

import com.github.zjzcn.ceper.utils.BaseBean;

public class SourceEvent  extends BaseBean {

	private static final long serialVersionUID = 1L;

	/*
	 * Source Name: set in FilterChain#process()
	 */
	private String sourceName;
	private String dataType;
	private String hashTag;
	private Map<String, Object> data;
	private long timestamp;
	Map<String, Object> attachments;

	public SourceEvent() {
		timestamp = System.currentTimeMillis();
		attachments = new HashMap<String, Object>();
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getHashTag() {
		return hashTag;
	}

	public void setHashTag(String hashTag) {
		this.hashTag = hashTag;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimstamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public Map<String, Object> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, Object> attachments) {
		this.attachments = attachments;
	}

	public void addAttachment(String key, Object value) {
		if (attachments == null) {
			attachments = new HashMap<String, Object>();
		}
		attachments.put(key, value);
	}

}