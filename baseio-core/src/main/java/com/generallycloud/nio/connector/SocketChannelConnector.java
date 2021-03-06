package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.MessageFormatter;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.UnsafeSocketSession;
import com.generallycloud.nio.component.concurrent.Waiter;

//FIXME 重连的时候不需要重新加载BaseContext
public final class SocketChannelConnector extends AbstractChannelConnector {

	private SocketChannelContext	context;

	private UnsafeSocketSession	session;

	private Waiter<Object> waiter = new Waiter<Object>();

	public SocketChannelConnector(SocketChannelContext context) {
		this.context = context;
	}

	public SocketSession connect() throws IOException {

		this.service();

		return getSession();
	}

	protected void connect(InetSocketAddress socketAddress) throws IOException {

		((SocketChannel) this.selectableChannel).connect(socketAddress);

		initSelectorLoops();

		if (waiter.await(getTimeout())) {

			CloseUtil.close(this);

			throw new TimeoutException("connect to " + socketAddress.toString() + " time out");
		}

		Object o = waiter.getPayload();

		if (o instanceof Exception) {
			
			CloseUtil.close(this);

			Exception t = (Exception) o;

			throw new TimeoutException(MessageFormatter.format("connect faild,connector:[{}],nested exception is {}",
					socketAddress, t.getMessage()), t);
		}
	}

	protected void finishConnect(UnsafeSocketSession session, Exception exception) {

		if (exception == null) {

			this.session = session;

			this.waiter.setPayload(null);

			if (waiter.isTimeouted()) {
				CloseUtil.close(this);
			}
		} else {

			this.waiter.setPayload(exception);
		}
	}
	
	protected boolean canSafeClose() {
		return session == null || (!session.inSelectorLoop() && !session.getEventLoop().inEventLoop());
	}

	protected void fireSessionOpend() {
		session.fireOpend();
	}

	public SocketChannelContext getContext() {
		return context;
	}

	public SocketSession getSession() {
		return session;
	}

	protected void initselectableChannel() throws IOException {

		this.selectableChannel = SocketChannel.open();

		this.selectableChannel.configureBlocking(false);
	}

	protected SelectorLoop newSelectorLoop(SelectorLoop[] selectorLoops) throws IOException {
		return new ClientSocketChannelSelectorLoop(this, selectorLoops);
	}

}
