package com.github.zjzcn.ceper.processor;

public abstract class AbstractProcessor implements Processor {

	private String name;
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
}