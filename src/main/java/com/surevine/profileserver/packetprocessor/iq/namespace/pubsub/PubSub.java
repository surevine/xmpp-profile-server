package com.surevine.profileserver.packetprocessor.iq.namespace.pubsub;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.namespace.AbstractNamespace;

public class PubSub extends AbstractNamespace {

	public static final String NAMESPACE_URI = "http://jabber.org/protocol/pubsub";

	private final PacketProcessor<IQ> setProcessor;

	public PubSub(BlockingQueue<Packet> outQueue, Properties conf,
			DataStore dataStore) {

		super(outQueue, conf, dataStore);
		setProcessor = new Set(outQueue, conf, dataStore);
	}

	@Override
	protected PacketProcessor<IQ> get() {
		return null;
	}

	@Override
	protected PacketProcessor<IQ> set() {
		return setProcessor;
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