package com.surevine.profileserver.packetprocessor.iq.namespace.vcard;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.namespace.AbstractNamespace;

public class VCard extends AbstractNamespace {

	public static final String NAMESPACE_URI = "urn:ietf:params:xml:ns:vcard-4.0";

	public static final String NONE = "none";

	private final PacketProcessor<IQ> getProcessor;

	public VCard(BlockingQueue<Packet> outQueue, Properties conf,
			DataStore dataStore) {

		super(outQueue, conf, dataStore);
		getProcessor = new Get(outQueue, conf, dataStore);
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
