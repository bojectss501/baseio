package com.generallycloud.nio.container.rtp.server;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.rtp.RTPContext;
import com.generallycloud.nio.component.DatagramChannel;

public class RTPJoinRoomServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPJoinRoomServlet.class.getSimpleName();

	public void doAccept(SocketSession session, ProtobaseReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPContext context = getRTPContext();

		Integer roomID = Integer.valueOf(future.getReadText());

		RTPRoomFactory roomFactory = context.getRTPRoomFactory();

		RTPRoom room = roomFactory.getRTPRoom(roomID);

//		DatagramChannel datagramChannel = session.getDatagramChannel();
		
		//FIXME udp 
		DatagramChannel datagramChannel = null;

		if (room == null || datagramChannel == null) {

			future.write(ByteUtil.FALSE);

			session.flush(future);
			
			return;
		}

		if (room.join(null)) {
			
			future.write(ByteUtil.TRUE);
			
		} else {

			future.write(ByteUtil.FALSE);
		}

		session.flush(future);

	}

}
