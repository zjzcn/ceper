package com.github.zjzcn.ceper.processor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.github.zjzcn.ceper.event.ResultEvent;
import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.rule.Defination;
import com.github.zjzcn.ceper.rule.Rule;
import com.github.zjzcn.ceper.rule.RuleListener;
import com.github.zjzcn.ceper.rule.RuleManager;
import com.typesafe.config.Config;

public class EsperProcessor extends AbstractProcessor {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final Integer DEFAULT_CAPACITY = 10000;
	
	private EPServiceProvider esperProvider;
	
	private BlockingQueue<ResultEvent> resultQueue;
	
	private int queueCapacity = DEFAULT_CAPACITY;
	
	@Override
	public String getProcessorType() {
		return "esper";
	}
	
	@Override
	public void config(Config config) {
		
	}

	@Override
	public void start() {
		logger.info("Starting EsperProcessor, name={}.", getName());
		resultQueue = new LinkedBlockingQueue<ResultEvent>(queueCapacity);
		esperProvider = EPServiceProviderManager.getDefaultProvider();
		final EPAdministrator admin = esperProvider.getEPAdministrator();

		RuleManager.subscribe(new RuleListener() {
			@Override
			public void childhanged(Set<Rule> rules) {
				addRulesToEsper(admin, rules);
			}

		});
		
		Set<Rule> rules = RuleManager.getRules();
		addRulesToEsper(admin, rules);
		logger.info("Started EsperProcessor, name={}.", getName());
	}

	@Override
	public void stop() {
		esperProvider.destroy();
	}

	@Override
	public void process(SourceEvent event) {
		EPRuntime runtime = esperProvider.getEPRuntime();
		runtime.sendEvent(event.getData(), event.getDataType());
	}

	@Override
	public ResultEvent getResult() {
		try {
			return resultQueue.take();
		} catch (InterruptedException e) {
			// NOOP
		}
		return null;
	}
	

	private void addRulesToEsper(final EPAdministrator admin, Set<Rule> rules) {
		for(final Rule rule : rules) {
			if(!getProcessorType().equals(rule.getProcessorType())) {
				continue;
			}
			for(Defination def : rule.getDefinations()) {
				if(admin.getConfiguration().isEventTypeExists(def.getDataType())) {
					admin.getConfiguration().updateMapEventType(def.getDataType(), def.getFieldMap());
					logger.info("Update rule definaton to Esper, defination={}", def);
				} else {
					admin.getConfiguration().addEventType(def.getDataType(), def.getFieldMap());
					logger.info("Add rule definaton to Esper, defination={}", def);
				}
			}
			EPStatement state = admin.createEPL(rule.getStatement(), rule.getStatementId());
			logger.info("Create EPL to Esper, rule={}", rule);
			state.addListener(new UpdateListener() {
				@SuppressWarnings("unchecked")
				@Override
				public void update(EventBean[] newEvents, EventBean[] oldEvents) {
					if (newEvents != null && newEvents.length > 0) {
						List<Map<String, Object>> events = new LinkedList<>();
						for(EventBean event : newEvents) {
							events.add((Map<String, Object>)event.getUnderlying());
						}
						ResultEvent resultEvent = new ResultEvent();
						resultEvent.setProcessorType(rule.getProcessorType());
						resultEvent.setStatementId(rule.getStatementId());
						resultEvent.setResult(events);
						try {
							resultQueue.put(resultEvent);
						} catch (InterruptedException e) {
							// NOOP
						}
					}
				}
			});
		}
	}
}