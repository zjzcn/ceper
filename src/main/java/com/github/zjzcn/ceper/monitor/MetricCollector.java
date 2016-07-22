package com.github.zjzcn.ceper.monitor;

import java.util.List;

public abstract class MetricCollector {

	public MetricCollector() {
		MonitorManager.registerCollector(this);
	}
	
	public abstract List<Metric> collect();
	
}