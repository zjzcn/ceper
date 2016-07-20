package com.github.zjzcn.ceper.router.route;

import java.util.Set;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.router.node.Node;
import com.typesafe.config.Config;

public class LocalRouteStrategy implements RouteStrategy {

	@Override
	public void config(Config config) {
		
	}
	
	@Override
	public Node route(SourceEvent event, Set<Node> endpoints) {
		for(Node ep : endpoints) {
			if(ep.isCurrent()) {
				return ep;
			}
		}
		return null;
	}

}
