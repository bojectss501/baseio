package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.protocol.DatagramPacket;

public class DatagramChannelSelectionReader implements SelectionAcceptor {

	private DatagramChannelContext		context		= null;
	private DatagramChannelSelectorLoop	selectorLoop	= null;
	private ByteBuf					cacheBuffer	= null;
	private Logger						logger		= LoggerFactory
			.getLogger(DatagramChannelSelectionReader.class);

	public DatagramChannelSelectionReader(DatagramChannelSelectorLoop selectorLoop) {
		this.selectorLoop = selectorLoop;
		this.context = selectorLoop.getContext();
		this.cacheBuffer = UnpooledByteBufAllocator.getInstance().allocate(DatagramPacket.PACKET_MAX);
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		DatagramChannelContext context = this.context;

		ByteBuf buf = this.cacheBuffer;

		buf.clear();

		DatagramChannel channel = (DatagramChannel) selectionKey.channel();

		ByteBuffer nioBuffer = buf.nioBuffer();
		
		InetSocketAddress remoteSocketAddress = (InetSocketAddress) channel.receive(nioBuffer);
		
		buf.skipBytes(nioBuffer.position());

		DatagramPacket packet = new DatagramPacket(buf);

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();

		if (acceptor == null) {
			logger.debug("______________ none acceptor for context");
			return;
		}
		
		DatagramSessionManager manager = context.getSessionManager();

		acceptor.accept(manager.getSession(selectorLoop, channel, remoteSocketAddress), packet);

	}
	
}
