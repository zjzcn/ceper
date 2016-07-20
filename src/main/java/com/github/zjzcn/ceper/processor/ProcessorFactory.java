package com.github.zjzcn.ceper.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.utils.Assert;
import com.github.zjzcn.ceper.utils.ClassUtils;

public class ProcessorFactory {

	private static final Logger logger = LoggerFactory.getLogger(ProcessorFactory.class);
	
	public static Processor create(String type) {
		Assert.notBlank(type, "type must have value");
		logger.info("Creating instance of Processor, type {}", type);
		try {
			Processor processor = null;
			switch (type) {
			case "esper":
				processor = new EsperProcessor();
				break;
			case "outlier":
				processor = new OutlierDetectionProcessor();
				break;
			default:
				processor = (Processor) ClassUtils.newInstance(type);
				break;
			}
			return processor;
		} catch (Exception ex) {
			throw new RuntimeException("Unable to create Processor, type=" + type, ex);
		}
	}
	
}
