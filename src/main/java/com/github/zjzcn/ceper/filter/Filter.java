package com.github.zjzcn.ceper.filter;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.typesafe.config.Config;

public interface Filter {

	void config(Config config);

	void init();

	void close();

	SourceEvent filter(SourceEvent event);

}
