package com.github.zjzcn.ceper.filter;

import java.util.List;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.typesafe.config.Config;

public interface Converter {

	void config(Config config);

	void init();

	void close();

	List<SourceEvent> convert(Object rawData);
}
