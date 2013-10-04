package com.surevine.profileserver.packetprocessor.iq.namespace.register;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.namespace.AbstractNamespace;

public class Register extends AbstractNamespace {

	public static final String NAMESPACE_URI = "jabber:iq:register";

	private final PacketProcessor<IQ> setProcessor;

	public Register(BlockingQueue<Packet> outQueue, Properties configuration,
			DataStore dataStore) {

		super(outQueue, configuration, dataStore);
		setProcessor = new Set(outQueue, configuration, dataStore);
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
