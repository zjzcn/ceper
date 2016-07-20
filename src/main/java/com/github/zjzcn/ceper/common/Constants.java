package com.github.zjzcn.ceper.common;

public class Constants {

	public static final String DEFAULT_CLUSTER_NAME = "cluster1";
	public static final String DEFAULT_ZK_SERVERS = "localhost:2181";
	public static final int DEFAULT_ROUTE_PORT = 8404;
	
	public static final String ZK_ROOT = "/ceper";
	public static final String PATH_SEPARATOR = "/";
	public static final String ZK_NODE_PATH = "/node";
	public static final String ZK_RULE_PATH = "/rule";
	
	// endpoint path: /ceper/clusterName/node
	public static String nodePath(String clusterName) {
		return ZK_ROOT + PATH_SEPARATOR + clusterName  + ZK_NODE_PATH;
	}
	
	// epl path: /ceper/clusterName/ruel
	public static String rulePath(String clusterName) {
		return ZK_ROOT + PATH_SEPARATOR + clusterName  + ZK_RULE_PATH;
	}
	
}
