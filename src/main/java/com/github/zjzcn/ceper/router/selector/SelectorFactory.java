package com.github.zjzcn.ceper.router.selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.utils.Assert;
import com.github.zjzcn.ceper.utils.ClassUtils;

public class SelectorFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(SelectorFactory.class);
	
	public static ProcessorSelector create(String type) {
		Assert.notBlank(type, "type must have value");
		logger.info("Creating instance of ProcessorSelector, type {}", type);
		try {
			ProcessorSelector selector = null;
			switch (type) {
			case "dynamic":
				selector = new DynamicDataTypeSelector();
				break;
			case "static":
				selector = new StaticSourceSelector();
				break;
			default:
				selector = (ProcessorSelector) ClassUtils.newInstance(type);
				break;
			}
			return selector;
		} catch (Exception ex) {
			throw new RuntimeException("Unable to create ProcessorSelector, type: " + type, ex);
		}
	}
}
