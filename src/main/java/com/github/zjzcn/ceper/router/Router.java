package com.github.zjzcn.ceper.router;

import java.net.BindException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.common.Constants;
import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.processor.Processor;
import com.github.zjzcn.ceper.router.node.Node;
import com.github.zjzcn.ceper.router.node.NodeListener;
import com.github.zjzcn.ceper.router.node.NodeManager;
import com.github.zjzcn.ceper.router.route.RouteStrategy;
import com.github.zjzcn.ceper.router.route.RouteStrategyFactory;
import com.github.zjzcn.ceper.router.selector.ProcessorSelector;
import com.github.zjzcn.ceper.router.selector.SelectorFactory;
import com.github.zjzcn.ceper.transport.Client;
import com.github.zjzcn.ceper.transport.ClientPool;
import com.github.zjzcn.ceper.transport.MessageHandler;
import com.github.zjzcn.ceper.transport.Protocol;
import com.github.zjzcn.ceper.transport.Request;
import com.github.zjzcn.ceper.transport.Response;
import com.github.zjzcn.ceper.transport.Server;
import com.github.zjzcn.ceper.transport.netty.NettyServer;
import com.github.zjzcn.ceper.utils.Assert;
import com.typesafe.config.Config;

public class Router {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private RouteStrategy routeStrategy;
	
	private ProcessorSelector selector;
	private Collection<Processor> processors;
	private NodeManager nodeManager;
	
	private Server server;
	
	private int routePort = Constants.DEFAULT_ROUTE_PORT;
	
	private Set<Node> cachedNodes = new CopyOnWriteArraySet<>();
	
	private NodeListener listener;
	
	public void config(Config config) {
		Config routeConfig = config.getConfig("route_strategy");
		String routeType = routeConfig.getString("type");
		routeStrategy = RouteStrategyFactory.create(routeType);
		routeStrategy.config(routeConfig);
		
		Config selectorConfig = config.getConfig("processor_selector");
		String selectorType = selectorConfig.getString("type");
		selector = SelectorFactory.create(selectorType);
		selector.config(selectorConfig);
		
		nodeManager = new NodeManager();
		nodeManager.config(config);
	}
	
	public void start() {
		server = new NettyServer(new MessageHandler() {
			@Override
			public void handleResponse(Response response) {
				// NOOP
			}
			@Override
			public Response handleRequest(Request request) {
				logger.debug("Request: {}", request);
				if(request.getData() instanceof SourceEvent) {
					// process remoting event
					processInternal((SourceEvent)request.getData());
				}
				return Protocol.buildMessageResponse(request.getRequestId());
			}
		});
		for(;;) {
			try {
				server.bind(routePort);
				break;
			} catch (BindException e) {
				logger.warn("Router port[{}] used so port add one, current port={}", routePort, routePort+1);
				routePort++;
			}
		}
		
		nodeManager.setRoutePort(routePort);
		nodeManager.start();
		cachedNodes.addAll(nodeManager.getNodes());
		listener = nodeManager.subscribe(new NodeListener() {
			@Override
			public void childhanged(Set<Node> nodes) {
				cachedNodes.clear();
				cachedNodes.addAll(nodes);
			}
		});
		
		selector.start();
	}
	
	public void stop() {
		nodeManager.unsubscribe(listener);
		server.close();
	}
	
	public void proccess(SourceEvent event) {
		Assert.notNull(event, "Event must not be null");

		Node node = routeStrategy.route(event, cachedNodes);
		if(node == null) {
			logger.debug("Not node for routing, routeStrategy={}, cachedNodes={}", routeStrategy.getClass().getSimpleName(), cachedNodes);
			return;
		}
		if(node.isCurrent()) {
			processInternal(event);
		} else {
			ClientPool pool = ClientPool.getPool(node.getHost(), node.getPort());
			Client client = pool.getResource();
			try {
				Request req = Protocol.buildMessageRequest(event);
				client.send(req);
			} catch (Exception e) {
				logger.error("Error while event routing to node[{}].", node, e);
			} finally {
				pool.returnResource(client);
			}
		}
	}
	
	public Collection<Processor> getProcessors() {
		return processors;
	}

	public void setProcessors(Collection<Processor> processors) {
		this.processors = processors;
	}
	
	private void processInternal(SourceEvent event) {
		Processor processor = selector.select(event, processors);
		processor.process(event);
	}

}
