package com.github.zjzcn.ceper.router.route;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.router.node.Node;
import com.github.zjzcn.ceper.utils.ConsistentHash;
import com.typesafe.config.Config;

/**
 * consistent hashing
 * @author zjzcn
 *
 */
public class HashRouteStrategy implements RouteStrategy {

	private static final Logger logger = LoggerFactory.getLogger(HashRouteStrategy.class);
	
	private ConsistentHash<Node> hash;
	
	private Set<Node> cachedNodes = new CopyOnWriteArraySet<>();
	
	@Override
	public void config(Config config) {
		hash = new ConsistentHash<Node>();
	}
	
	@Override
	public Node route(SourceEvent event, Set<Node> nodes) {
		if(!equels(nodes, cachedNodes)) {
			cachedNodes.clear();
			cachedNodes.addAll(nodes);
			hash.removeAll();
			hash.addAll(nodes);
		}
		if(event.getHashTag() == null && event.getDataType() == null) {
			logger.warn("DataType and hashTag is null, can not route. event={}", event);
			return null;
		} else if(event.getHashTag() == null && event.getDataType() != null) {
			logger.debug("Event hashtag is null, use dataType={} as hashtag.", event.getDataType());
			event.setHashTag(event.getDataType());
		}
		Node node = hash.get(event.getHashTag());
		if(node == null) {
			logger.debug("Hash result is null, hashtag={}, nodes={}", event.getHashTag(), hash.getAll());
		}
		return node;
	}

	private boolean equels(Set<Node> nodes1, Set<Node> nodes2) {
		if(nodes1.size() != nodes2.size()) {
			return false;
		}
		
		if(nodes1.containsAll(nodes2) && nodes2.containsAll(nodes1)) {
			return true;
		} else {
			return false;
		}
	}
}
