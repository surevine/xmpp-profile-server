package com.surevine.profileserver.packetprocessor.iq.namespace.vcard;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;

public class Get implements PacketProcessor<IQ> {

	private BlockingQueue<Packet> outQueue;
	private Properties configuration;
	private NodeStore nodeStore;

	public Get(BlockingQueue<Packet> outQueue, Properties configuration,
			NodeStore nodeStore) {
		this.outQueue      = outQueue;
		this.configuration = configuration;
		this.nodeStore     = nodeStore;
	}

	@Override
	public void process(IQ packet) throws Exception {


	}

}
