package com.github.zjzcn.ceper.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class JsonUtils {

	/**
	 * Object to JSON String
	 * @param obj: [Map/Array/Collection/Bean(plain)]
	 * @return JSON String
	 */
	public static String toJsonString(Object obj) {
		return JSON.toJSONString(obj);
	}

	/**
	 * JSON String convert to Bean(plain)
	 * @param jsonString
	 * @param clazz
	 * @return T
	 */
	public static <T> T toBean(String jsonString, Class<T> clazz) {
		return  JSON.parseObject(jsonString, clazz);
	}

	/**
	 * JSON String convert to Bean(plain) List
	 * @param jsonString
	 * @param clazz
	 * @return T
	 */
	public static <T> List<T> toList(String jsonString, Class<T> clazz) {
		return  JSON.parseArray(jsonString, clazz);
	}

	/**
	 * JSON String convert to List<Map<String, Object>>
	 * @param jsonString
	 * @return List<Map<String, Object>>
	 */
	public static List<Object> toList(String jsonString) {
		return  JSON.parseArray(jsonString);
	}
	
	/**
	 * JSON String convert to Map
	 * @param jsonString
	 * @return Map
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String jsonString) {
		return JSON.parseObject(jsonString, Map.class);
	}
	
	//--------------test-------------------
	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("username", "wanglihong");
		map.put("height", 12.5);
		map.put("age", 24);
		String jsonString = toJsonString(map);
		System.out.println(jsonString);
		
		System.out.println(toMap(jsonString));
		
		
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		list.add(map);
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("ssddd", "dddd");
		m.put("lll", "lll");
		l.add(m);
		map.put("ddd", l);
		String json = toJsonString(list);
		System.out.println(json);
		System.out.println(toJsonString(toList(json, User.class)));
		System.out.println(toList(json));
		toMap("{age:24, username:'wanglihong', height:12.5}");
	}
	
	public static class User {
		private String username;
		private double height;
		private int age;
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public double getHeight() {
			return height;
		}
		public void setHeight(double height) {
			this.height = height;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
	}
}
