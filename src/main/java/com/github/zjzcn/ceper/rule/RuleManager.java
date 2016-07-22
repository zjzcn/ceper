package com.github.zjzcn.ceper.rule;

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
import com.github.zjzcn.ceper.utils.ZkClient;
import com.typesafe.config.Config;

public class RuleManager {

	private static final Logger logger = LoggerFactory.getLogger(RuleManager.class);

	private static String zkServers = Constants.DEFAULT_ZK_SERVERS;

	private static String clusterName = Constants.DEFAULT_CLUSTER_NAME;

	private static Set<RuleListener> listeners = new CopyOnWriteArraySet<RuleListener>();

	private static ZkClient zkClient;

	private static PathChildrenCache cache;
	
	public static void config(Config config) {
		if (config.hasPath("cluster_name")) {
			clusterName = config.getString("cluster_name");
		}
		if (config.hasPath("zk_servers")) {
			zkServers = config.getString("zk_servers");
		}
	}

	public static void start() {
		logger.info("Starting RuleManager.");
		zkClient = ZkClient.getClient(zkServers);

		// add watch
		final String path = Constants.rulePath(clusterName);
		cache = zkClient.getChildrenCache(path);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				logger.info("PathChildrenCacheEvent fired, path={}, eventType={}", path, event.getType());
				if(event.getType() == Type.CHILD_ADDED 
						|| event.getType() == Type.CHILD_UPDATED 
						|| event.getType() == Type.CHILD_REMOVED) {
					
					notifyListeners();
				}
			}
		});
		try {
			cache.start(StartMode.BUILD_INITIAL_CACHE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		logger.info("Started RuleManager.");
	}

	public static void stop() {
		listeners.clear();
	}

	public static RuleListener subscribe(RuleListener listener) {
		listeners.add(listener);
		return listener;
	}

	public static void unsubscribe(RuleListener listener) {
		listeners.remove(listener);
	}

	public static Set<Rule> getRules() {
		Set<Rule> list = new HashSet<>();
		List<ChildData> childDatas = cache.getCurrentData();
		for (ChildData childData : childDatas) {
			Rule n = convertRule(childData);
			list.add(n);
		}
		logger.info("Get rules form cache, rules={}", list);
		return list;
	}

	private static void notifyListeners() {
		Set<Rule> rules = getRules();
		for (RuleListener listener : listeners) {
			try {
				listener.childhanged(rules);
			} catch (Exception e) {
				logger.error("Listener Exception.", e);
			}
		}
	}

	private static Rule convertRule(ChildData childData) {
		String data = new String(childData.getData());
		Rule rule = JsonUtils.toBean(data, Rule.class);
		return rule;
	}
}
