package com.github.zjzcn.ceper.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

public class ZkClient {
	
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final int DEFAULT_SESSION_TIMEOUT_MS = 60 * 1000;
	private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 15 * 1000;
	private static final int CONNECT_RETRY_INTERVAL_MS = 5000;
	private static final String DEFAULT_DATA = "";

	private static final Map<String, ZkClient> clients = new ConcurrentHashMap<String, ZkClient>();
	
	// Map<path, cache>
	private final ConcurrentMap<String, PathChildrenCache> childrenCaches = new ConcurrentHashMap<String, PathChildrenCache>();
	// Map<path, cache>
	private final ConcurrentMap<String, NodeCache> nodeCaches = new ConcurrentHashMap<String, NodeCache>();
	
	private CuratorFramework curator;
	
	private String zkServers;
	
	private ZkClient(String zkServers) {
		this(zkServers, null, null);
	}

	private ZkClient(String zkServers, String username, String password) {
		this.zkServers = zkServers;
		List<AuthInfo> authInfos = new ArrayList<AuthInfo>();
		if (username != null && password != null) {
			AuthInfo authInfo = new AuthInfo("digest", (username + ":" + password).getBytes());
			authInfos.add(authInfo);
		}
		curator = CuratorFrameworkFactory.builder()
				.defaultData(DEFAULT_DATA.getBytes())
				.connectString(zkServers)
				.retryPolicy(new RetryNTimes(Integer.MAX_VALUE, CONNECT_RETRY_INTERVAL_MS))
				.sessionTimeoutMs(DEFAULT_SESSION_TIMEOUT_MS)
				.connectionTimeoutMs(DEFAULT_CONNECTION_TIMEOUT_MS)
				.authorization(authInfos).build();
		curator.start();
	}
	
	public static ZkClient getClient(String zkServers) {
		return getClient(zkServers, null, null);
	}
	
	public synchronized static ZkClient getClient(String zkServers, String username, String password) {
		ZkClient client = clients.get(zkServers);
		if(client == null) {
			client = new ZkClient(zkServers, username, password);
			clients.put(zkServers, client);
		}
		return client;
	}
	
	public CuratorFramework getCurator() {
		return this.curator;
	}

	public int countChildren(String path) {
		try {
			Stat stat = new Stat();
			this.readData(path, stat);
			return stat.getNumChildren();
		} catch (Exception e) {
			return -1;
		}
	}

	public void create(String path, byte[] data, CreateMode mode) {
		try {
			curator.create().withMode(mode).forPath(path, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void createEphemeral(String path) {
		try {
			curator.create().withMode(CreateMode.EPHEMERAL).forPath(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void createEphemeral(String path, String data) {
		try {
			curator.create().withMode(CreateMode.EPHEMERAL).forPath(path, data.getBytes(DEFAULT_CHARSET));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void createEphemeralSequential(String path) {
		try {
			curator.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void createPersistent(String path) {
		try {
			curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
		} catch (NodeExistsException e) {
			// nothing
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void createPersistent(String path, String data) {
		try {
			curator.create().withMode(CreateMode.PERSISTENT).forPath(path, data.getBytes(DEFAULT_CHARSET));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void createPersistentSequential(String path, String data) {
		try {
			curator.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, data.getBytes(DEFAULT_CHARSET));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean delete(String path) {
		try {
			curator.delete().forPath(path);
			return true;
		} catch (NoNodeException e) {
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean deleteRecursive(String path) {
		try {
			curator.delete().deletingChildrenIfNeeded().forPath(path);
			return true;
		} catch (NoNodeException e) {
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean exists(String path) {
		try {
			Stat stat = curator.checkExists().forPath(path);
			return stat != null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * children list, if NoNode return null;
	 * @param path
	 * @return
	 */
	public List<String> getChildren(String path) {
		try {
			return curator.getChildren().forPath(path);
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String readData(String path) {
		try {
			byte[] data = curator.getData().forPath(path);
			return data==null ? null : new String(data, DEFAULT_CHARSET);
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String readData(String path, Stat stat) {
		try {
			byte[] data = curator.getData().storingStatIn(stat).forPath(path);
			return data==null ? null : new String(data, DEFAULT_CHARSET);
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public NodeCache getNodeCache(String path) {
		NodeCache cache = nodeCaches.putIfAbsent(path, new NodeCache(curator, path, true));
		try {
			if(cache == null) {
				cache = nodeCaches.get(path);
				cache.start(true);
			}
			return cache;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public PathChildrenCache getChildrenCache(String path) {
		PathChildrenCache cache = childrenCaches.putIfAbsent(path, new PathChildrenCache(curator, path, true));
		try {
			if(cache == null) {
				cache = childrenCaches.get(path);
//				cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
			}
			return cache;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Stat writeData(String path, String data) {
		try {
			if(!exists(path)) {
				createPersistent(path);
			}
			return curator.setData().forPath(path, data.getBytes(DEFAULT_CHARSET));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Stat writeData(String path, String data, int expectedVersion) {
		try {
			return curator.setData().withVersion(expectedVersion).forPath(path, data.getBytes(DEFAULT_CHARSET));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param path
	 * @param perms
	 *            see {@link ZooDefs.Perms}
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public void setDigestACL(String path, int perms, String username, String password) throws Exception {
		Id id = new Id("digest", DigestAuthenticationProvider.generateDigest(username + ":" + password));
		ACL acl = new ACL(perms, id);
		List<ACL> acls = new ArrayList<ACL>();
		acls.add(acl);
		curator.setACL().withACL(acls).forPath(path);
	}

	public void close() {
		clients.remove(zkServers);
		if(curator != null) {
			curator.close();
		}
	}
	
	public boolean isStarted() {
		return curator.getState() == CuratorFrameworkState.STARTED;
	}
	
	public static void main(String[] args) {
		ZkClient client = new ZkClient("localhost:2181", "zjz", "123");
		client.createPersistent("/admin/test/dddd");
		
	}
}
