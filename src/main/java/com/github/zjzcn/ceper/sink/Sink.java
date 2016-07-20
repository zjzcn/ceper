package com.github.zjzcn.ceper.sink;

import java.util.List;

import com.github.zjzcn.ceper.event.ResultEvent;
import com.github.zjzcn.ceper.processor.Processor;
import com.typesafe.config.Config;

public interface Sink {

	void setName(String name);

	String getName();
	
	void config(Config config);
	
	void start();
	
	void stop();
	
	void process(ResultEvent event);
	
	void setProcessors(List<Processor> processors);
	
	List<Processor> getProcessors();

}
