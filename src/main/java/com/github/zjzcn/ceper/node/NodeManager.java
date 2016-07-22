package com.github.zjzcn.ceper.node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.common.Constants;
import com.github.zjzcn.ceper.utils.Assert;
import com.github.zjzcn.ceper.utils.JsonUtils;
import com.github.zjzcn.ceper.utils.NetUtils;
import com.github.zjzcn.ceper.utils.ZkClient;
import com.typesafe.config.Config;

public class NodeManager {

	private static final Logger logger = LoggerFactory.getLogger(NodeManager.class);

	private static final Set<NodeListener> listeners = new CopyOnWriteArraySet<NodeListener>();

	private static Node currentNode;

	private static ZkClient zkClient;

	private static String clusterName;
	
	private static String zkServers;

	private static PathChildrenCache cache;
	
	public static void config(Config config) {
		if (config.hasPath("cluster_name")) {
			clusterName = config.getString("cluster_name");
		} else {
			clusterName = Constants.DEFAULT_CLUSTER_NAME;
		}

		if (config.hasPath("zk_servers")) {
			zkServers = config.getString("zk_servers");
		} else {
			zkServers = Constants.DEFAULT_ZK_SERVERS;
		}
	}

	public static void start() {
		logger.info("Starting NodeManager.");

		zkClient = ZkClient.getClient(zkServers);
		// add watch
		final String path = Constants.nodePath(clusterName);
		cache = zkClient.getChildrenCache(path);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				logger.info("PathChildrenCacheEvent fired, path={}, eventType={}", path, event.getType());
				if(event.getType() == Type.CONNECTION_RECONNECTED && currentNode != null) {
					register(currentNode);
					cache.rebuild();
				}
				notifyListeners();
			}
		});
		try {
			cache.start(StartMode.BUILD_INITIAL_CACHE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		logger.info("Started NodeManager.");
	}

	public static void stop() {
		listeners.clear();
	}

	public static void registerCurrentNode(int nodePort) {
		String host = NetUtils.getLocalAddress().getHostAddress();
		currentNode = new Node();
		currentNode.setClusterName(clusterName);
		currentNode.setHost(host);
		currentNode.setPort(nodePort);
		register(currentNode);
	}
	
	public static boolean isCurrentNode(Node node) {
		Assert.notNull(currentNode);
		Assert.notNull(node);
		return node.equals(currentNode);
	}
	
	public static void register(Node node) {
		String nodeData = JsonUtils.toJsonString(node);
		String nodePath = Constants.nodePath(clusterName);
		if (!zkClient.exists(nodePath)) {
			zkClient.createPersistent(nodePath);
		}
		String tmpNode = nodePath + Constants.PATH_SEPARATOR + node.toNodeId();
		if (zkClient.exists(tmpNode)) {
			zkClient.delete(tmpNode);
		}
		zkClient.createEphemeral(tmpNode, nodeData);
		logger.info("Node registered to zookeeper, path={}, node={}", tmpNode, nodeData);
	}

	public static void unregister(Node node) {
		String nodePath = Constants.nodePath(clusterName);
		if (!zkClient.exists(nodePath)) {
			return;
		}
		String tmpNode = nodePath + Constants.PATH_SEPARATOR + node.toString();
		zkClient.delete(tmpNode);
	}

	public static NodeListener subscribe(NodeListener listener) {
		listeners.add(listener);
		return listener;
	}

	public static void unsubscribe(NodeListener listener) {
		listeners.remove(listener);
	}

//	public static void setRoutePort(int port) {
//		routePort = port; 
//	}

	
	public static Node getCurrentNode() {
		return currentNode;
	}

	public static Set<Node> getNodes() {
		Set<Node> nodes = new HashSet<>();
		List<ChildData> childDatas = cache.getCurrentData();
		for (ChildData childData : childDatas) {
			Node n = convertNode(childData);
			nodes.add(n);
		}
		nodes.add(currentNode);
		return nodes;
	}
	
	private static void notifyListeners() {
		Set<Node> nodes = getNodes();
		logger.debug("Get nodes form cache, nodes={}", nodes);
		for (NodeListener listener : listeners) {
			try {
				listener.childhanged(nodes);
			} catch (Exception e) {
				logger.error("Listener Exception.", e);
			}
		}
	}

	private static Node convertNode(ChildData childData) {
		String data = new String(childData.getData());
		Node node = JsonUtils.toBean(data, Node.class);
		return node;
	}
}
