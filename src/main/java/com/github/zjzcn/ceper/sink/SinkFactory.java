package com.github.zjzcn.ceper.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.utils.Assert;
import com.github.zjzcn.ceper.utils.ClassUtils;

public class SinkFactory {

	private static final Logger logger = LoggerFactory.getLogger(SinkFactory.class);
	
	public static Sink create(String type) {
		Assert.notBlank(type, "type must have value");
		logger.info("Creating instance of sink, type {}", type);
		try {
			Sink sink = null;
			switch (type) {
			case "kafka":
				sink = new KafkaSink();
				break;
			case "logger":
				sink = new LoggerSink();
				break;
			default:
				sink = (Sink) ClassUtils.newInstance(type);
				break;
			}
			return sink;
		} catch (Exception ex) {
			throw new RuntimeException("Unable to create sink, type: " + type, ex);
		}
	}
}
