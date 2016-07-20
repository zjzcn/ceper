package com.github.zjzcn.ceper.transport.serializer;

public class SerializerFactory {

	public static Serializer create() {
		return new FstSerializer();
	}
}
