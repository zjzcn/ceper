package com.github.zjzcn.ceper.transport.serialization;

public interface Serializer {

	public byte[] serialize(Object obj);
	
	public <T> T deserialize(byte[] bytes, Class<T> clazz);
}
