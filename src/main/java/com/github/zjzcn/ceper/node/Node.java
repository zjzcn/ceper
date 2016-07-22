package com.github.zjzcn.ceper.node;

import com.github.zjzcn.ceper.utils.BaseBean;

public class Node extends BaseBean {

	private static final long serialVersionUID = 1L;
	
	private String clusterName;
	private String host;
	private int port;
	
	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String toNodeId() {
		return host + ":" + port;
	}
	
	@Override
	public int hashCode() {
		return host.hashCode() * 37 + port;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		Node o = (Node)obj;
		return host.equals(o.host) && port== o.port;
	}
}
