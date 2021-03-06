package com.generallycloud.nio.codec.protobase.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class ProtobaseBeatFutureFactory implements BeatFutureFactory {

	public ReadFuture createPINGPacket(SocketSession session) {
		return new ProtobaseReadFutureImpl(session.getContext()).setPING();
	}

	public ReadFuture createPONGPacket(SocketSession session) {
		return new ProtobaseReadFutureImpl(session.getContext()).setPONG();
	}

}
