package com.github.zjzcn.ceper.processor;

import java.util.concurrent.BlockingQueue;

import com.github.zjzcn.ceper.event.ResultEvent;
import com.github.zjzcn.ceper.event.SourceEvent;
import com.typesafe.config.Config;

public class OutlierDetectionProcessor extends AbstractProcessor {
	
	private BlockingQueue<SourceEvent> queue;
	
	@Override
	public String getProcessorType() {
		return "outlier";
	}
	
	@Override
	public void config(Config config) {
		
	}

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		
	}

	@Override
	public void process(SourceEvent event) {
		try {
			queue.put(event);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ResultEvent getResult() {
		// TODO Auto-generated method stub
		return null;
	}

}