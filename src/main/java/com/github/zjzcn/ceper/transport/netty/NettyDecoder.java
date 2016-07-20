package com.github.zjzcn.ceper.transport.netty;

import java.util.List;

import com.github.zjzcn.ceper.transport.Protocol;
import com.github.zjzcn.ceper.transport.Response;
import com.github.zjzcn.ceper.transport.serialization.Serializer;
import com.github.zjzcn.ceper.transport.serialization.SerializerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class NettyDecoder extends ByteToMessageDecoder {

	private Serializer serializer = SerializerFactory.create();
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() <= Protocol.MESSAGE_HEADER_LENGTH) {
			return;
		}

		in.markReaderIndex();

		int magic = in.readUnsignedShort();
		if (magic != Protocol.MESSAGE_MAGIC) {
			in.resetReaderIndex();
			throw new RuntimeException("NettyDecoder transport header not support, magic: " + magic);
		}

		byte messageType = in.readByte();
		//协议扩展 1byte 保留
		@SuppressWarnings("unused")
		byte ext = in.readByte();
		long requestId = in.readLong();

		int dataLength = in.readInt();

		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}

		byte[] data = new byte[dataLength];

		in.readBytes(data);
		
		try {
			out.add(serializer.deserialize(data, Object.class));
		} catch (Exception e) {
			if (messageType == Protocol.MessageType.HEARTBEAT_REQ) {
				Response resonse = Protocol.buildHeartbeatResponse(requestId);
				ctx.write(resonse);
			} else if(messageType == Protocol.MessageType.MESSAGE_REQ) {
				Response resonse = Protocol.buildMessageResponse(requestId);
				resonse.setException(e);
				ctx.write(resonse);
			}
			throw e;
		}
	}


}
