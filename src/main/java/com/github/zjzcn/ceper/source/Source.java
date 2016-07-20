package com.github.zjzcn.ceper.source;

import com.github.zjzcn.ceper.filter.FilterChain;
import com.typesafe.config.Config;

public interface Source {

	void setName(String name);

	String getName();

	void config(Config config);

	void start();

	void stop();

	void setFilterChain(FilterChain filterChain);

	FilterChain getFilterChain();
}
