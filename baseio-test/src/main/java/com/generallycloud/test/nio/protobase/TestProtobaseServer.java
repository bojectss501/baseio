package com.generallycloud.test.nio.protobase;

import com.generallycloud.nio.container.protobase.startup.ProtobaseServerStartup;

public class TestProtobaseServer {

	public static void main(String[] args) throws Exception {
		
		ProtobaseServerStartup s = new ProtobaseServerStartup();
		
		s.launch("nio");
	}
}
