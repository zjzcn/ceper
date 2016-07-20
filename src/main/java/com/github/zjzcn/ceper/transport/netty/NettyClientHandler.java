package com.github.zjzcn.ceper.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.transport.MessageHandler;
import com.github.zjzcn.ceper.transport.Protocol;
import com.github.zjzcn.ceper.transport.Request;
import com.github.zjzcn.ceper.transport.Response;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

	private MessageHandler messageHandler;
	
	public NettyClientHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// heartbeat handle
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if(e.state() == IdleState.WRITER_IDLE) {
            	// client send heartbeat
            	Request heartbeat = Protocol.buildHeartbeatRequest();
            	ctx.writeAndFlush(heartbeat);
            	logger.debug("NettyClient sent heartbeat request, requestId={}", heartbeat.getRequestId());
            } else if(e.state() == IdleState.READER_IDLE) {
            	// client response timeout
            	logger.error("NettyClient receive heartbeat response timeout, channel will be closed. localAddress={}, remoteAddress={}",  ctx.channel().localAddress(), ctx.channel().remoteAddress());
            	ctx.close();
            }
        }
    }
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof Request) {
			Request request = (Request) msg;
			long processStartTime = System.currentTimeMillis();
			try {
				Response response = messageHandler.handleRequest(request);
				response.setProcessTime(System.currentTimeMillis() - processStartTime);
				if (ctx.channel().isActive()) {
					ctx.write(response);
				}
			} catch (Exception e) {
				String errorMsg = "NettyHandler handle requset error.";
				logger.error(errorMsg, e);
				Response response = new Response();
				response.setRequestId(request.getRequestId());
				response.setException(new RuntimeException(errorMsg, e));
				response.setProcessTime(System.currentTimeMillis() - processStartTime);
				ctx.write(response);
			}
		} else if (msg instanceof Response) {
			Response response = (Response)msg;
			if(response.getMessageType() != Protocol.MessageType.HEARTBEAT_RESP) {
				messageHandler.handleResponse(response);
			}
		} else {
			String errorMsg = "NettyHandler messageReceived type not support: class=" + msg.getClass();
			logger.error(errorMsg);
			throw new RuntimeException(errorMsg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
		logger.error("Exception:", t);
		ctx.close();
	}

}
