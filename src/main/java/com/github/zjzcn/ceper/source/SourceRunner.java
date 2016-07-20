package com.github.zjzcn.ceper.source;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceRunner {

	private static final Logger logger = LoggerFactory.getLogger(SourceRunner.class);

	private static final long MAX_BACKOFF_SLEEP = 5000;

	private Source source;

	private AtomicBoolean shouldStop = new AtomicBoolean(false);

	private Thread runnerThread;

	public SourceRunner(Source source) {
		this.source = source;
	}
	
	public void start() {
		source.start();
		// only PollingSource
		if (source instanceof PollingSource) {
			final PollingSource pollingSource = (PollingSource) source;
			runnerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					logger.info("Polling runner starting. Source={}", source.getName());
					while (!shouldStop.get()) {
						try {
							pollingSource.process();
						} catch (Exception e) {
							logger.error("Unhandled exception, logging and sleeping for " + MAX_BACKOFF_SLEEP + "ms", e);
							try {
								Thread.sleep(MAX_BACKOFF_SLEEP);
							} catch (InterruptedException ex) {
								Thread.currentThread().interrupt();
							}
						}
					}
				}

			});
			runnerThread.setName("SourceRunner");
			runnerThread.start();
		}
	}

	public void stop() {
		if (source instanceof PollingSource) {
			shouldStop.set(true);
			try {
				runnerThread.interrupt();
				runnerThread.join();
			} catch (InterruptedException e) {
				logger.warn("Interrupted while waiting for polling runner to stop.", e);
				Thread.currentThread().interrupt();
			}
		}
		source.stop();
	}

}
