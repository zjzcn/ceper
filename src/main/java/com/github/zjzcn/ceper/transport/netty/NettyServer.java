package com.github.zjzcn.ceper.transport.netty;

import java.net.BindException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.transport.MessageHandler;
import com.github.zjzcn.ceper.transport.Protocol;
import com.github.zjzcn.ceper.transport.Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyServer implements Server {
	
	private static Logger logger = LoggerFactory.getLogger(NettyServer.class);

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	private ServerBootstrap bootstrap;
	private Channel channel;
	private SocketAddress localAddress;
	
	public NettyServer(int bossThreads, int workerThreads, final MessageHandler messageHandler) {
		if(bossThreads > 0) {
			bossGroup = new NioEventLoopGroup(bossThreads);
		} else {
			bossGroup = new NioEventLoopGroup();
		}
		if(workerThreads > 0) {
			workerGroup = new NioEventLoopGroup(workerThreads);
		} else {
			workerGroup = new NioEventLoopGroup();
		}
		
		bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 20000)
            // 设置no_delay
			.option(ChannelOption.TCP_NODELAY, true)
			// 设置channel no_delay
			.childOption(ChannelOption.TCP_NODELAY, true)
			// 设置可以重用time_wait的socket
			.childOption(ChannelOption.SO_REUSEADDR, true)
			.childOption(ChannelOption.SO_KEEPALIVE, false)
			// 设置SO_LINGER为2秒
			.childOption(ChannelOption.SO_LINGER, 2)
			.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast("decoder", new NettyEncoder());
					pipeline.addLast("encoder", new NettyDecoder());
					pipeline.addLast("heartbeat", new IdleStateHandler(Protocol.SERVER_IDLE_TIMEOUT, 0, 0, TimeUnit.SECONDS));
					pipeline.addLast("handler", new NettyServerHandler(messageHandler));
				}
			});
	}
	
	public NettyServer(MessageHandler messageHandler) {
		this(0, 0, messageHandler);
	}
	
	@Override
	public void bind(int serverPort) throws BindException {
		this.bind(null, serverPort);
	}

	@Override
	public void bind(String serverHost, int serverPort) throws BindException {
		logger.info("Netty server binding.");
		try {
			if(serverHost == null) {
				channel = bootstrap.bind(serverPort).sync().channel();
			} else {
				channel = bootstrap.bind(serverHost, serverPort).sync().channel();
			}
			localAddress = channel.localAddress();
			logger.info("Netty server is bound. listen at [{}].", channel.localAddress());
		} catch (Exception e) {
			if(e instanceof BindException) {
				throw (BindException)e;
			} else {
				logger.error("Netty server error occurred while starting.", e);
			}
		}
	}
	
	@Override
	public void close() {
		logger.info("Netty server closing.");
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		channel.close();
		logger.info("Netty server closed.");
	}

	@Override
	public SocketAddress getLocalAddress() {
		return localAddress;
	}

	@Override
	public boolean isBound() {
		return channel.isActive();
	}

}
