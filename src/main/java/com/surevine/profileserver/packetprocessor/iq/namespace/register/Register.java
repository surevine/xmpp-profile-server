package com.surevine.profileserver.packetprocessor.iq.namespace.register;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.packetprocessor.iq.NamespaceProcessorAbstract;

public class Register extends NamespaceProcessorAbstract {

	public static final String NAMESPACE_URI = "jabber:iq:register";
	
	public Register(BlockingQueue<Packet> outQueue, Properties configuration,
			NodeStore nodeStore) {
		super(outQueue, configuration, nodeStore);

	}
	
	@Override
	public void process(IQ packet) throws Exception {
		// TODO Auto-generated method stub

	}

}
