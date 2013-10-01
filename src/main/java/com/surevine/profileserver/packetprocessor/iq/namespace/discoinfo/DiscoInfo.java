package com.surevine.profileserver.packetprocessor.iq.namespace.discoinfo;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.namespace.AbstractNamespace;

public class DiscoInfo extends AbstractNamespace {

	public static final String NAMESPACE_URI = "http://jabber.org/protocol/disco#info";

	private final PacketProcessor<IQ> getProcessor;

	public DiscoInfo(BlockingQueue<Packet> outQueue, Properties conf,
			NodeStore nodeStore) {

		super(outQueue, conf, nodeStore);
		getProcessor = new Get(outQueue, nodeStore);
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
