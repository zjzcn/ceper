package com.github.zjzcn.ceper.transport;

import java.io.Serializable;
import java.util.Map;

import com.github.zjzcn.ceper.utils.JsonUtils;

public class Request implements Serializable {

	private static final long serialVersionUID = 1L;

	private long requestId;
	private byte messageType;
	private Object data;
	Map<String, Object> attachments;

	public byte getMessageType() {
		return messageType;
	}

	public void setMessageType(byte messageType) {
		this.messageType = messageType;
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Map<String, Object> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, Object> attachments) {
		this.attachments = attachments;
	}
 
	@Override
	public String toString() {
		return JsonUtils.toJsonString(this);
	}
}
