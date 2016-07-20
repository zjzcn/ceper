package com.github.zjzcn.ceper.transport.serializer;

public interface Serializer {

	public byte[] serialize(Object obj);
	
	public <T> T deserialize(byte[] bytes, Class<T> clazz);
}
