package com.github.zjzcn.ceper.utils;

import java.io.Closeable;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public abstract class Pool<T> implements Closeable {

	protected GenericObjectPool<T> internalPool;

	public Pool() {
		// NOOP
	}

	public Pool(final GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {
		initPool(poolConfig, factory);
	}

	public void initPool(final GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {
		if (this.internalPool != null) {
			try {
				destroy();
			} catch (Exception e) {
				// NOOP
			}
		}
		this.internalPool = new GenericObjectPool<T>(factory, poolConfig);
	}

	public T getResource() {
		try {
			return internalPool.borrowObject();
		} catch (Exception e) {
			throw new RuntimeException("Could not get a resource from the pool", e);
		}
	}

	public void returnResource(final T resource) {
		if (resource == null) {
			return;
		}
		try {
			internalPool.returnObject(resource);
		} catch (Exception e) {
			throw new RuntimeException("Could not return the resource to the pool", e);
		}
	}

	public void returnBrokenResource(T resource) {
		if (resource == null) {
			return;
		}
		try {
			internalPool.invalidateObject(resource);
		} catch (Exception e) {
			throw new RuntimeException("Could not return the resource to the pool", e);
		}
	}

	public void destroy() {
		try {
			internalPool.close();
		} catch (Exception e) {
			throw new RuntimeException("Could not destroy the pool", e);
		}
	}

	@Override
	public void close() {
		destroy();
	}

	public int getNumActive() {
		if (isInactive()) {
			return -1;
		}
		return this.internalPool.getNumActive();
	}

	public int getNumIdle() {
		if (isInactive()) {
			return -1;
		}
		return this.internalPool.getNumIdle();
	}

	public int getNumWaiters() {
		if (isInactive()) {
			return -1;
		}

		return this.internalPool.getNumWaiters();
	}

	public long getMeanBorrowWaitTimeMillis() {
		if (isInactive()) {
			return -1;
		}

		return this.internalPool.getMeanBorrowWaitTimeMillis();
	}

	public long getMaxBorrowWaitTimeMillis() {
		if (isInactive()) {
			return -1;
		}

		return this.internalPool.getMaxBorrowWaitTimeMillis();
	}

	public void addObjects(int count) {
		try {
			for (int i = 0; i < count; i++) {
				this.internalPool.addObject();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error trying to add idle objects", e);
		}
	}

	public boolean isClosed() {
		return this.internalPool.isClosed();
	}

	private boolean isInactive() {
		return this.internalPool == null || this.internalPool.isClosed();
	}
}
