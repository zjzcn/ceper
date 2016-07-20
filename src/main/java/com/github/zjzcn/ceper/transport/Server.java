package com.github.zjzcn.ceper.transport;

import java.net.BindException;
import java.net.SocketAddress;

public interface Server {
	
	void bind(int serverPort) throws BindException;
	
	void bind(String serverHost, int serverPort) throws BindException;
	
	void close();
	
    SocketAddress getLocalAddress();

    boolean isBound();

}
