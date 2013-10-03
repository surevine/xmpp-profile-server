package com.surevine.profileserver.packetprocessor.iq.namespace.register;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.namespace.AbstractNamespace;

public class Register extends AbstractNamespace {

	public static final String NAMESPACE_URI = "jabber:iq:register";

	private final PacketProcessor<IQ> getProcessor;

	public Register(BlockingQueue<Packet> outQueue, Properties configuration,
			NodeStore nodeStore) {

		super(outQueue, configuration, nodeStore);
		getProcessor = new Get(outQueue, configuration, nodeStore);
	}

	@Override
	protected PacketProcessor<IQ> get() {
		return getProcessor;
	}

	@Override
	protected PacketProcessor<IQ> set() {
		return null;
	}

	@Override
	protected PacketProcessor<IQ> result() {
		return null;
	}

	@Override
	protected PacketProcessor<IQ> error() {
		return null;
	}
}
