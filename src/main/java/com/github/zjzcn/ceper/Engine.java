package com.github.zjzcn.ceper;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.filter.FilterChain;
import com.github.zjzcn.ceper.processor.Processor;
import com.github.zjzcn.ceper.processor.ProcessorFactory;
import com.github.zjzcn.ceper.router.Router;
import com.github.zjzcn.ceper.rule.RuleManager;
import com.github.zjzcn.ceper.sink.Sink;
import com.github.zjzcn.ceper.sink.SinkFactory;
import com.github.zjzcn.ceper.sink.SinkRunner;
import com.github.zjzcn.ceper.source.Source;
import com.github.zjzcn.ceper.source.SourceFactory;
import com.github.zjzcn.ceper.source.SourceRunner;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class Engine {

	private static final Logger logger = LoggerFactory.getLogger(Engine.class);

	private static final String CONFIG_FILE = "conf/config.conf";
	private static final String LOGBACK_XML_FILE = "conf/logback.xml";
	
	private final Map<String, Processor> processors = new HashMap<String, Processor>();;
	private final Map<String, SourceRunner> sourceRunners = new HashMap<String, SourceRunner>();
	private final Map<String, SinkRunner> sinkRunners = new HashMap<String, SinkRunner>();

	private RuleManager ruleManager;
	private Router router;
	
	public static void main(String[] args) {
		try {
			URL url = Engine.class .getProtectionDomain().getCodeSource().getLocation();
			String rootPath = URLDecoder.decode(url.getPath(), "utf-8");
			if(rootPath.endsWith(".jar")) {
				rootPath = rootPath.substring(0, rootPath.lastIndexOf("/lib/") + 1);
			}
			
			File logbackFile = new File(rootPath + LOGBACK_XML_FILE);
			if (logbackFile.exists()) {
				LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(lc);
				lc.reset();
				try {
					configurator.doConfigure(logbackFile);
				}
				catch (JoranException e) {
					e.printStackTrace(System.err);
					System.exit(-1);
				}
			}
			
			logger.info("Engine starting.");
			File configFile = new File(rootPath + CONFIG_FILE);
			Config config = ConfigFactory.parseFile(configFile);
			logger.info("Load config from {}, config={}.", configFile.getCanonicalPath(), config);
			
			final Engine engine = new Engine();
			engine.config(config);
			engine.start();

			Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook") {
				@Override
				public void run() {
					engine.stop();
				}
			});
			
			logger.info("Engine started.");
		} catch (Exception e) {
			logger.error("A fatal error occurred while running.", e);
			System.exit(1);
		}
	}

	public void config(Config rootConfig) {
		ruleManager = RuleManager.getInstance();
		ruleManager.config(rootConfig);
		
		router = new Router();
		router.config(rootConfig);

		List<? extends Config> procConfigs = rootConfig.getConfigList("processors");
		for (Config conf : procConfigs) {
			String type = conf.getString("type");
			String name = conf.getString("name");
			Processor processor = ProcessorFactory.create(type);
			processor.setName(name);
			processor.config(conf);
			processors.put(name, processor);
		}
		router.setProcessors(processors.values());

		List<? extends Config> srcConfigs = rootConfig.getConfigList("sources");
		for (Config conf : srcConfigs) {
			String type = conf.getString("type");
			String name = conf.getString("name");
			Source source = SourceFactory.create(type);
			source.setName(name);
			source.config(conf);

			FilterChain filterChain = new FilterChain(source, router);
			filterChain.config(rootConfig);
			source.setFilterChain(filterChain);
			sourceRunners.put(name, new SourceRunner(source));
		}

		List<? extends Config> sinkConfigs = rootConfig.getConfigList("sinks");
		for (Config sinkConfig : sinkConfigs) {
			String name = sinkConfig.getString("name");
			String type = sinkConfig.getString("type");
			Sink sink = SinkFactory.create(type);
			sink.setName(name);
			sink.config(sinkConfig);

			List<String> procNames = sinkConfig.getStringList("processors");
			List<Processor> sinkProcessers = new LinkedList<>();
			for(String procName : procNames) {
				Processor processor = processors.get(procName);
				sinkProcessers.add(processor);
			}
			sink.setProcessors(sinkProcessers);
			sinkRunners.put(name, new SinkRunner(sink));
		}
	}

	public void start() {
		ruleManager.start();
		
		for (Entry<String, Processor> entry : processors.entrySet()) {
			try {
				entry.getValue().start();
			} catch (Exception e) {
				logger.error("Error while starting Processor {}", entry.getValue(), e);
			}
		}

		for (Entry<String, SinkRunner> entry : sinkRunners.entrySet()) {
			try {
				entry.getValue().start();
			} catch (Exception e) {
				logger.error("Error while starting SinkRunner  {}", entry.getValue(), e);
			}
		}

		router.start();
		
		for (Entry<String, SourceRunner> entry : sourceRunners.entrySet()) {
			try {
				entry.getValue().start();
			} catch (Exception e) {
				logger.error("Error while starting SourceRunner {}", entry.getValue(), e);
			}
		}
	}

	public void stop() {
		router.stop();
		ruleManager.stop();
		
		for (Entry<String, SourceRunner> entry : sourceRunners.entrySet()) {
			try {
				entry.getValue().start();
			} catch (Exception e) {
				logger.error("Error while Stoping {}", entry.getValue(), e);
			}
		}

		for (Entry<String, SinkRunner> entry : sinkRunners.entrySet()) {
			try {
				entry.getValue().start();
			} catch (Exception e) {
				logger.error("Error while Stoping {}", entry.getValue(), e);
			}
		}

		for (Entry<String, Processor> entry : processors.entrySet()) {
			try {
				entry.getValue().start();
			} catch (Exception e) {
				logger.error("Error while Stoping Processor {}", entry.getValue(), e);
			}
		}
	}

}
