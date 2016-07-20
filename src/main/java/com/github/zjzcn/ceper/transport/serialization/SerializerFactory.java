package com.github.zjzcn.ceper.transport.serialization;

public class SerializerFactory {

	public static Serializer create() {
		return new FstSerializer();
	}
}
