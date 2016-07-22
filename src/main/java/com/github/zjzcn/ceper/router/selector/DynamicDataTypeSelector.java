package com.github.zjzcn.ceper.router.selector;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.processor.Processor;
import com.github.zjzcn.ceper.rule.Defination;
import com.github.zjzcn.ceper.rule.Rule;
import com.github.zjzcn.ceper.rule.RuleListener;
import com.github.zjzcn.ceper.rule.RuleManager;
import com.typesafe.config.Config;

public class DynamicDataTypeSelector implements ProcessorSelector {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// Map<dataType, processorName>
	private Map<String, String> mappings = new ConcurrentHashMap<String, String>();
		
	private RuleListener listener;
	@Override
	public void config(Config config) {
		
	}
	
	@Override
	public void start() {
		listener = RuleManager.subscribe(new RuleListener() {
			@Override
			public void childhanged(Set<Rule> rules) {
				mappings.clear();
				addMappings(rules);
			}
		});
		mappings.clear();
		Set<Rule> rules = RuleManager.getRules();
		addMappings(rules);
	}

	@Override
	public void stop() {
		RuleManager.unsubscribe(listener);
		mappings.clear();
	}
	
	@Override
	public Processor select(SourceEvent event, Collection<Processor> processors) {
		String procName = mappings.get(event.getDataType());
		if(procName == null) {
			logger.warn("Not found mapping info, type={}, mappings={}", event.getDataType(), mappings);
			return null;
		}
		for(Processor processor : processors) {
			if(procName.equals(processor.getName())) {
				return processor;
			}
		}
		logger.warn("Not found processor, processorName={}, processors={}", procName, processors);
		return null;
	}

	private void addMappings(Set<Rule> rules) {
		for(Rule rule : rules) {
			for(Defination def : rule.getDefinations()) {
				mappings.put(def.getDataType(), rule.getProcessorType());
			}
		}
	}
}
