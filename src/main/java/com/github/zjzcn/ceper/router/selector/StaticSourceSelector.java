package com.github.zjzcn.ceper.router.selector;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.processor.Processor;
import com.typesafe.config.Config;

public class StaticSourceSelector implements ProcessorSelector {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// Map<sourceName, processorName>
	private Map<String, String> mappings = new HashMap<String, String>();
	
	@Override
	public void config(Config config) {
		List<? extends Config> selectConfigs = config.getConfigList("mappings");
		for(Config conf : selectConfigs) {
			String source = conf.getString("source");
			String processor = conf.getString("processor");
			mappings.put(source, processor);
		}
	}
	
	@Override
	public void start() {
		// NOOP
	}

	@Override
	public void stop() {
		mappings.clear();
	}
	
	@Override
	public Processor select(SourceEvent event, Collection<Processor> processors) {
		String procName = mappings.get(event.getSourceName());
		if(procName == null) {
			logger.warn("Not found mapping info, source={}, mappingTables={}", event.getSourceName(), mappings);
			return null;
		}
		for(Processor proc : processors) {
			if(procName.equals(proc.getName())) {
				return proc;
			}
		}
		logger.warn("Not found processor, processorName={}, processorList={}", procName, processors);
		return null;
	}

}