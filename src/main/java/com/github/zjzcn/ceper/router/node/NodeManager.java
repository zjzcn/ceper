package com.github.zjzcn.ceper.router.node;

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
import com.github.zjzcn.ceper.utils.JsonUtils;
import com.github.zjzcn.ceper.utils.NetUtils;
import com.github.zjzcn.ceper.utils.ZkClient;
import com.typesafe.config.Config;

public class NodeManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Set<NodeListener> listeners = new CopyOnWriteArraySet<NodeListener>();

	private int routePort;
	
	private Node currentNode;

	private ZkClient zkClient;

	private String clusterName;
	
	private String zkServers;

	private PathChildrenCache cache;
	

	public void config(Config config) {
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

	public void start() {
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
					registerCurrentNode();
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
		currentNode = registerCurrentNode();
		logger.info("Started NodeManager.");
	}

	public void stop() {
		listeners.clear();
	}

	public void register(Node node) {
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

	public void unregister(Node node) {
		String nodePath = Constants.nodePath(clusterName);
		if (!zkClient.exists(nodePath)) {
			return;
		}
		String tmpNode = nodePath + Constants.PATH_SEPARATOR + node.toString();
		zkClient.delete(tmpNode);
	}

	public NodeListener subscribe(NodeListener listener) {
		listeners.add(listener);
		return listener;
	}

	public void unsubscribe(NodeListener listener) {
		listeners.remove(listener);
	}

	public void setRoutePort(int port) {
		this.routePort = port; 
	}

	
	public Node getCurrentNode() {
		return currentNode;
	}

	public Set<Node> getNodes() {
		Set<Node> nodes = new HashSet<>();
		List<ChildData> childDatas = cache.getCurrentData();
		for (ChildData childData : childDatas) {
			Node n = convertNode(childData);
			nodes.add(n);
		}
		nodes.add(currentNode);
		logger.info("Get nodes form cache, nodes={}", nodes);
		return nodes;
	}
	
	private Node registerCurrentNode() {
		String host = NetUtils.getLocalAddress().getHostAddress();
		Node currentNode = new Node();
		currentNode.setCurrent(true);
		currentNode.setClusterName(clusterName);
		currentNode.setHost(host);
		currentNode.setPort(routePort);
		this.register(currentNode);
		return currentNode;
	}

	private void notifyListeners() {
		Set<Node> nodes = getNodes();
		for (NodeListener listener : listeners) {
			try {
				listener.childhanged(nodes);
			} catch (Exception e) {
				logger.error("Listener Exception.", e);
			}
		}
	}

	private Node convertNode(ChildData childData) {
		String data = new String(childData.getData());
		Node node = JsonUtils.toBean(data, Node.class);
		return node;
	}
}
