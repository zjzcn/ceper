package com.github.zjzcn.ceper.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash<T> {

	public static final int DEFAULT_CONSISTENT_HASH_REPLICAS = 1000;
	 
	private final SortedMap<Integer, T> circle = new TreeMap<Integer, T>();
	private final HashFunction hashFunction;
	private final int numberOfReplicas;
	
	public ConsistentHash() {
		this(DEFAULT_CONSISTENT_HASH_REPLICAS, new HashSet<T>());
	}
	
	public ConsistentHash(int numberOfReplicas) {
		this(numberOfReplicas, new HashSet<T>());
	}
	
	public ConsistentHash(int numberOfReplicas, Collection<T> nodes) {
		this.hashFunction = new HashFunction();
		this.numberOfReplicas = numberOfReplicas;

		for (T node : nodes) {
			add(node);
		}
	}

	public void add(T node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			circle.put(hashFunction.hash(node.toString() + i), node);
		}
	}

	public void addAll(Collection<T> nodes) {
		for (T node : nodes) {
			add(node);
		}
	}
	
	public void remove(T node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			circle.remove(hashFunction.hash(node.toString() + i));
		}
	}
	
	public void removeAll() {
		circle.clear();
	}

	public boolean contains(T node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			if(circle.containsKey(hashFunction.hash(node.toString() + i))) {
				return true;
			}
		}
		return false;
	}
	
	public T get(Object key) {
		if (circle.isEmpty()) {
			return null;
		}
		int hash = hashFunction.hash(key);
		// System.out.println("hash---: " + hash);
		if (!circle.containsKey(hash)) {
			SortedMap<Integer, T> tailMap = circle.tailMap(hash);
			hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
		}
		// System.out.println("hash---: " + hash);
		return circle.get(hash);
	}

	public Collection<T> getAll() {
		return circle.values();
	}
	
	public static class HashFunction {
		int hash(Object key) {
			// md5加密后，hashcode
			return Md5Utils.md5Hex(key.toString()).hashCode();
		}
	}

	public static void main(String[] args) {
		HashSet<String> set = new HashSet<String>();
		set.add("A");
		set.add("B");
		set.add("C");
		set.add("D");

		Map<String, Integer> map = new HashMap<String, Integer>();

		ConsistentHash<String> consistentHash = new ConsistentHash<String>(1000, set);

		int count = 10000;

		for (int i = 0; i < count; i++) {
			String key = consistentHash.get(i);
			if (map.containsKey(key)) {
				map.put(consistentHash.get(i), map.get(key) + 1);
			} else {
				map.put(consistentHash.get(i), 1);
			}
			// System.out.println(key);
		}

		showServer(map);
		map.clear();
		consistentHash.remove("A");

		System.out.println("------- remove A");

		for (int i = 0; i < count; i++) {
			String key = consistentHash.get(i);
			if (map.containsKey(key)) {
				map.put(consistentHash.get(i), map.get(key) + 1);
			} else {
				map.put(consistentHash.get(i), 1);
			}
			// System.out.println(key);
		}

		showServer(map);
		map.clear();
		consistentHash.add("E");
		System.out.println("------- add E");

		for (int i = 0; i < count; i++) {
			String key = consistentHash.get(i);
			if (map.containsKey(key)) {
				map.put(consistentHash.get(i), map.get(key) + 1);
			} else {
				map.put(consistentHash.get(i), 1);
			}
			// System.out.println(key);
		}

		showServer(map);
		map.clear();

		consistentHash.add("F");
		System.out.println("------- add F服务器  业务量加倍");
		count = count * 2;
		for (int i = 0; i < count; i++) {
			String key = consistentHash.get(i);
			if (map.containsKey(key)) {
				map.put(consistentHash.get(i), map.get(key) + 1);
			} else {
				map.put(consistentHash.get(i), 1);
			}
			// System.out.println(key);
		}

		showServer(map);

	}

	public static void showServer(Map<String, Integer> map) {
		for (Entry<String, Integer> m : map.entrySet()) {
			System.out.println("服务器 " + m.getKey() + "----" + m.getValue() + "个");
		}
	}
}
