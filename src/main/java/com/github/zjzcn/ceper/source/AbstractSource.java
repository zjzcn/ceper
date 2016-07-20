package com.github.zjzcn.ceper.source;

import com.github.zjzcn.ceper.filter.FilterChain;

public abstract class AbstractSource implements Source {

	private String name;
	
	private FilterChain filterChain;
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setFilterChain(FilterChain filterChain) {
		this.filterChain = filterChain;
	}
	
	@Override
	public FilterChain getFilterChain() {
		return filterChain;
	}

}