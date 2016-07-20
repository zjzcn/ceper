package com.github.zjzcn.ceper.transport.serializer;

import java.util.Arrays;
import java.util.List;

import org.nustaq.serialization.FSTConfiguration;

public class FstSerializer implements Serializer {

	private FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
	
	@Override
	public byte[] serialize(Object obj) {
		return conf.asByteArray(obj);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		return (T)conf.asObject(bytes);
	}

	public static void main(String[] args) {
		byte[] b = new FstSerializer().serialize(Arrays.asList("dddd", "cccc"));
		List<?> r = new FstSerializer().deserialize(b, List.class);
		System.out.println(r);
	}
}
