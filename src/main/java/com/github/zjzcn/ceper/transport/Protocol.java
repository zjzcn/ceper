package com.github.zjzcn.ceper.transport;

/**
 * | magic | type | ext | request id | body length | body           |
 * +-------+------+-----+------------+-------------+----------------+
 * | 2byte |1byte |1byte| 8byte      | 4byte       |body length byte|
 * 
 * @author zjz
 *
 */
public class Protocol {
	
    public static final int MESSAGE_MAGIC = 0xCECE;
    public static final int MESSAGE_HEADER_LENGTH = 16;
    
    // heartbeat constants start
    public static final int CLIENT_HEARTBEAT_INTERVAL = 2;
    public static final int CLIENT_IDLE_TIMEOUT= 20;
    public static final int SERVER_IDLE_TIMEOUT = 60;
    
	// netty config value
	public static final int CLIENT_MAX_REQUEST = 2000;
	public static final int REQUEST_TIMEOUT_TIMER_PERIOD = 100;
    
	public static class MessageType {
		public static final byte HEARTBEAT_REQ = 1;
		public static final byte HEARTBEAT_RESP = 2;
		public static final byte MESSAGE_REQ = 3;
		public static final byte MESSAGE_RESP = 4;
	}
	
	public static Request buildHeartbeatRequest() {
		Request req = new Request();
        req.setRequestId(RequestId.newId());
        req.setMessageType(Protocol.MessageType.HEARTBEAT_REQ);
        return req;
	}
	
	public static Response buildHeartbeatResponse(long requestId) {
		Response resp = new Response();
        resp.setRequestId(requestId);
        resp.setMessageType(Protocol.MessageType.HEARTBEAT_RESP);
        return resp;
	}
	
	public static Request buildMessageRequest(Object data) {
		Request req = new Request();
        req.setRequestId(RequestId.newId());
        req.setMessageType(Protocol.MessageType.MESSAGE_REQ);
        req.setData(data);
        return req;
	}
	
	public static Response buildMessageResponse(long requestId) {
		Response resp = new Response();
        resp.setRequestId(requestId);
        resp.setMessageType(Protocol.MessageType.MESSAGE_RESP);
        return resp;
	}
}
