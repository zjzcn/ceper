package com.github.zjzcn.ceper.transport;

import java.io.Serializable;
import java.util.Map;

public class Response implements Serializable {

	private static final long serialVersionUID = 1L;

	private long requestId;
	private byte messageType;
	private Object data;
	Map<String, Object> attachments;
	private Exception exception;
	private long processTime;
	private int timeout;

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

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public long getProcessTime() {
		return processTime;
	}

	public void setProcessTime(long processTime) {
		this.processTime = processTime;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
