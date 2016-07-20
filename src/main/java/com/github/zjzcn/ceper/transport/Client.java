package com.github.zjzcn.ceper.transport;

import java.net.SocketAddress;

public interface Client {

	void connect();
	
	void reconnect();
	
	void close();
	
	SocketAddress getLocalAddress();

	SocketAddress getRemoteAddress();
	
	Response send(Request req);
	
	boolean isConnected();
	
}
