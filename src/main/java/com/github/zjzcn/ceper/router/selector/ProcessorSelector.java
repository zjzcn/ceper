package com.github.zjzcn.ceper.router.selector;

import java.util.Collection;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.processor.Processor;
import com.typesafe.config.Config;

public interface ProcessorSelector {
	
	void config(Config config);
	
	void start();
	
	void stop();
	
	Processor select(SourceEvent event, Collection<Processor> processors);
}
