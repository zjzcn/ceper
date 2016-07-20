package com.github.zjzcn.ceper.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.event.ResultEvent;
import com.typesafe.config.Config;

public class LoggerSink extends AbstractSink {

	private static final Logger logger = LoggerFactory.getLogger(LoggerSink.class);

	@Override
	public void config(Config config) {
		
	}

	@Override
	public void start() {
		logger.info("LoggerSink started.");
	}

	@Override
	public void stop() {
		logger.info("LoggerSink stoped.");
	}

	@Override
	public void process(ResultEvent event) {
		logger.info("Event: {}", event);
	}
	
}
