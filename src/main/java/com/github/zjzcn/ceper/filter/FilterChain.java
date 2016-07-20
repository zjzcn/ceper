package com.github.zjzcn.ceper.filter;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.router.Router;
import com.github.zjzcn.ceper.source.Source;
import com.github.zjzcn.ceper.utils.ClassUtils;
import com.typesafe.config.Config;

public class FilterChain {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Converter converter;
	
	private List<Filter> filters = new LinkedList<Filter>();

	private Source source;
	
	private Router router;
	
	public FilterChain(Source source, Router router) {
		this.source = source;
		this.router = router;
	}
	
	public void config(Config config) {
		if(config.hasPath("converter")) {
			Config converterConfig = config.getConfig("converter");
			String type = converterConfig.getString("type");
			try {
				switch (type) {
				case "json":
					converter = new JsonConverter();
					break;
				default:
					converter = (Converter) ClassUtils.newInstance(type);
					break;
				}
			} catch (Exception ex) {
				throw new RuntimeException("Unable to create Converter, type: " + type, ex);
			}
		} else {
			converter = new JsonConverter();
		}
		try {
			List<? extends Config> filterConfigs = config.getConfigList("filters");
			for(Config filterConfig : filterConfigs) {
				String type = filterConfig.getString("type");
				logger.info("Creating source filter, type {}", type);
				Filter filter = null;
				switch (type) {
				case "timestamp":
					//						filter = new ReplicatingChannelSelector();
					break;
				default:
					filter = (Filter) ClassUtils.newInstance(type);
					break;
				}
				filter.config(filterConfig);
				filters.add(filter);
			}
		} catch (Exception e) {
			logger.warn("Has not source filters.");
		}
	}

	
	public void process(Object rawData) {
		String sourceName = source.getName();
		List<SourceEvent> events = converter.convert(rawData);
		if (events == null) {
			return;
		}
		for(SourceEvent event : events) {
			event.setSourceName(sourceName);
			for (Filter filter : filters) {
				event = filter.filter(event);
			}
			router.proccess(event);;
		}
	}

	public void init() {
		for (Filter filter : filters) {
			filter.init();
		}
	}

	public void close() {
		for (Filter filter : filters) {
			filter.close();
		}
	}

	
}
