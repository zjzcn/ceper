package com.github.zjzcn.ceper.processor;

import com.github.zjzcn.ceper.event.ResultEvent;
import com.github.zjzcn.ceper.event.SourceEvent;
import com.typesafe.config.Config;

public interface Processor {

	void config(Config config);
	
	void start();
	
	void stop();
	
	void process(SourceEvent event);
	
	ResultEvent getResult();

	void setName(String name);

	String getName();
	
	String getProcessorType();
}
