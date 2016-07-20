package com.github.zjzcn.ceper.event;

import java.util.HashMap;
import java.util.Map;

import com.github.zjzcn.ceper.utils.BaseBean;

public class ResultEvent extends BaseBean {

	private static final long serialVersionUID = 1L;

	private String processorType;
	private String statementId;
	private long timestamp;
	private Object result;
	Map<String, Object> attachments;

	public ResultEvent() {
		timestamp = System.currentTimeMillis();
		attachments = new HashMap<String, Object>();
	}

	public String getProcessorType() {
		return processorType;
	}

	public void setProcessorType(String processorType) {
		this.processorType = processorType;
	}

	public String getStatementId() {
		return statementId;
	}

	public void setStatementId(String statementId) {
		this.statementId = statementId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
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