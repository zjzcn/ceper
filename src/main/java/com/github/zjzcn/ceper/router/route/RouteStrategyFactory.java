package com.github.zjzcn.ceper.router.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.utils.Assert;
import com.github.zjzcn.ceper.utils.ClassUtils;

public class RouteStrategyFactory {

	private static final Logger logger = LoggerFactory.getLogger(RouteStrategyFactory.class);
	
	public static RouteStrategy create(String type) {
		Assert.notBlank(type, "type must have value");
		logger.info("Creating instance of RouteStrategy, type {}", type);
		try {
			RouteStrategy selector = null;
			switch (type) {
			case "hash":
				selector = new HashRouteStrategy();
				break;
			case "local":
				selector = new LocalRouteStrategy();
				break;
			default:
				selector = (RouteStrategy) ClassUtils.newInstance(type);
				break;
			}
			return selector;
		} catch (Exception ex) {
			throw new RuntimeException("Unable to create RouteStrategy, type: " + type, ex);
		}
	}
	
}
