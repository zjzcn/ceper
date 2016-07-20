package com.github.zjzcn.ceper.transport.netty;

import com.github.zjzcn.ceper.transport.Protocol;
import com.github.zjzcn.ceper.transport.Request;
import com.github.zjzcn.ceper.transport.Response;
import com.github.zjzcn.ceper.transport.serialization.Serializer;
import com.github.zjzcn.ceper.transport.serialization.SerializerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<Object> {

	private Serializer serializer = SerializerFactory.create();
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		
		long requestId = 0;
		byte messageType = 0;
		if(msg instanceof Request) {
			Request req = (Request)msg;
			requestId = req.getRequestId();
			messageType = req.getMessageType();
		} else if (msg instanceof Response) {
			Response resp = (Response)msg;
			requestId = resp.getRequestId();
			messageType = resp.getMessageType();
		}
		
		byte[] data = serializer.serialize(msg);
		
		out.writeShort(Protocol.MESSAGE_MAGIC);
		out.writeByte(messageType);
		out.writeByte(0);
		out.writeLong(requestId);
		out.writeInt(data.length);
		out.writeBytes(data);
	}

}
