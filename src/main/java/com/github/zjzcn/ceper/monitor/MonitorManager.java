package com.github.zjzcn.ceper.monitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.common.Constants;
import com.github.zjzcn.ceper.node.Node;
import com.github.zjzcn.ceper.node.NodeManager;
import com.github.zjzcn.ceper.utils.JsonUtils;
import com.github.zjzcn.ceper.utils.StringUtils;
import com.github.zjzcn.ceper.utils.ZkClient;
import com.typesafe.config.Config;

public class MonitorManager {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorManager.class);
	
	private static List<MetricCollector> collectors = new LinkedList<>();
	
	private static List<Metric> metrics = new LinkedList<>();
	
	private static ZkClient zkClient;

	private static String clusterName;
	
	private static String zkServers;

	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	
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
		logger.info("Starting MonitorManager.");
		zkClient = ZkClient.getClient(zkServers);

		Node currentNode = NodeManager.getCurrentNode();
		final String path = Constants.monitorPath(clusterName) + Constants.PATH_SEPARATOR + currentNode.toNodeId();
		String data = zkClient.readData(path);
		if(StringUtils.isNotBlank(data)) {
			List<Metric> list = JsonUtils.toList(data, Metric.class);
			metrics.addAll(list);
		}
		
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				List<Metric> list = new LinkedList<>();
				for(MetricCollector collector : collectors) {
					List<Metric> l = collector.collect();
					list.addAll(l);
				}
				zkClient.writeData(path, JsonUtils.toJsonString(list));
			}
		}, 30, 60, TimeUnit.SECONDS);
		
		logger.info("Started MonitorManager.");
	}
	
	public static void stop() {
		metrics.clear();
	}
	
	public static long getSourceCounter(String sourceName) {
		return 0;
	}
	
	public static void registerCollector(MetricCollector collector) {
		collectors.add(collector);
	}
}
