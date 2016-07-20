package com.github.zjzcn.ceper.router.route;

import java.util.Set;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.router.node.Node;
import com.typesafe.config.Config;

public interface RouteStrategy {
	
	void config(Config config);
	
	Node route(SourceEvent event, Set<Node> endpoints);
}
