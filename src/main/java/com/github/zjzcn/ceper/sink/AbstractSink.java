package com.github.zjzcn.ceper.sink;

import java.util.List;

import com.github.zjzcn.ceper.processor.Processor;

public abstract class AbstractSink implements Sink {

	private List<Processor> processors;

	private String name;
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setProcessors(List<Processor> processors) {
		this.processors = processors;
	}

	@Override
	public List<Processor> getProcessors() {
		return processors;
	}

	

}