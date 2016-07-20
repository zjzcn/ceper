package com.github.zjzcn.ceper.transport;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.github.zjzcn.ceper.transport.netty.NettyClient;
import com.github.zjzcn.ceper.utils.Pool;

public class ClientPool extends Pool<Client> {

    private static ConcurrentMap<String, ClientPool> clientPools = new ConcurrentHashMap<String, ClientPool>();
    
	public ClientPool(String serverHost, int serverPort) {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMinIdle(2);
		poolConfig.setMaxIdle(10);
		poolConfig.setMaxWaitMillis(200);
		poolConfig.setLifo(true);
		poolConfig.setMinEvictableIdleTimeMillis((long) 1000 * 60 * 60);//默认链接空闲时间
		poolConfig.setSoftMinEvictableIdleTimeMillis((long) 1000 * 60 * 10);
		poolConfig.setTimeBetweenEvictionRunsMillis((long) 1000 * 60 * 10);//默认回收周期
		super.initPool(poolConfig, new PooledClientFactory(serverHost, serverPort));
	}

    public static synchronized ClientPool getPool(String serverHost, int serverPort) {
    	String key = poolKey(serverHost, serverPort);
    	if(!clientPools.containsKey(key)) {
    		clientPools.put(key, new ClientPool(serverHost, serverPort));
    	}
    	ClientPool pool = clientPools.get(key);
    	return pool;
    }
    
    public static synchronized void deleteClientPool(String serverHost, int serverPort) {
    	String key = poolKey(serverHost, serverPort);
    	ClientPool pool = clientPools.remove(key);
    	if(pool != null) {
    		pool.close();
    	}
    }
    
    private static String poolKey(String serverHost, int serverPort) {
    	String key = serverHost + ":" + serverPort;
    	return key;
    }
    
	@Override
	public Client getResource() {
		return super.getResource();
	}

	@Override
	public void returnResource(Client resource) {
		if (resource == null) {
			return;
		}
		try {
			super.returnResource(resource);
		} catch (Exception e) {
			super.returnBrokenResource(resource);
			throw e;
		}
	}

	@Override
	public void returnBrokenResource(Client resource) {
		if (resource != null) {
			super.returnBrokenResource(resource);
		}
	}

	//----------------private class------------------
	private class PooledClientFactory implements PooledObjectFactory<Client> {
		private String host;
		private int port;
		
		public PooledClientFactory(String host, int port) {
		    this.host = host;
		    this.port = port;
		  }

		@Override
		public PooledObject<Client> makeObject() throws Exception {
			Client client = new NettyClient(host, port);
			client.connect();
			return new DefaultPooledObject<Client>(client);
		}

		@Override
		public void destroyObject(PooledObject<Client> pooledObj) throws Exception {
			Client client = pooledObj.getObject();
			client.close();
		}

		@Override
		public boolean validateObject(PooledObject<Client> pooledObj) {
			Client client = pooledObj.getObject();
			return client.isConnected();
		}

		@Override
		public void activateObject(PooledObject<Client> pooledObj) throws Exception {
			Client client = pooledObj.getObject();
			if (!client.isConnected()) {
				client.connect();
			}
		}

		@Override
		public void passivateObject(PooledObject<Client> p) throws Exception {
			// NOOP
		}

	}

}
