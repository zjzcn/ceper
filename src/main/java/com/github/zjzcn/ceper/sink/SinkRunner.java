package com.github.zjzcn.ceper.sink;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.event.ResultEvent;
import com.github.zjzcn.ceper.processor.Processor;

public class SinkRunner {

	private static final Logger logger = LoggerFactory.getLogger(SinkRunner.class);
	
	private static final long MAX_BACKOFF_SLEEP = 5000;

	private Sink sink;
	
	private Thread runnerThread;

	private AtomicBoolean shouldStop = new AtomicBoolean(false);
	
	public SinkRunner(Sink sink) {
		this.sink = sink;
	}
	
	public void start() {
		sink.start();
		
		runnerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("Polling sink runner starting");
				while (!shouldStop.get()) {
					List<Processor> processors =  sink.getProcessors();
					for(Processor processor : processors) {
						try {
							ResultEvent event = processor.getResult();
							if(event != null) {
								sink.process(event);
							}
						} catch (Exception e) {
							logger.error("Unable to deliver event. Exception follows.", e);
							try {
								Thread.sleep(MAX_BACKOFF_SLEEP);
							} catch (InterruptedException ex) {
								Thread.currentThread().interrupt();
							}
						}
					}
				}
				logger.info("Polling runner exiting. ");
			}
		});
		runnerThread.setName("SinkRunner");
		runnerThread.start();
	}

	public void stop() {
		if (runnerThread != null) {
			shouldStop.set(true);
			runnerThread.interrupt();
			while (runnerThread.isAlive()) {
				try {
					logger.info("Waiting for runner thread to exit");
					runnerThread.join(500);
				} catch (InterruptedException e) {
					logger.info("Interrupted while waiting for runner thread to exit. Exception follows.", e);
				}
			}
		}
	}

}
