package com.surevine.profileserver.packetprocessor.iq.namespace.command;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.namespace.AbstractNamespace;

public class Command extends AbstractNamespace {

	public static final String NAMESPACE_URI = "http://jabber.org/protocol/commands";
	public static final String GET_USER_ROSTER = "http://jabber.org/protocol/admin#get-user-roster";
	
	public static final String FORM_TYPE = "http://jabber.org/protocol/admin";
	
	private final Result resultProcessor;

	public Command(BlockingQueue<Packet> outQueue, Properties configuration,
			DataStore dataStore) {

		super(outQueue, configuration, dataStore);
		
		resultProcessor = new Result(outQueue, configuration, dataStore);
	}

	@Override
	protected PacketProcessor<IQ> get() {
		return null;
	}

	@Override
	protected PacketProcessor<IQ> set() {
		return null;
	}

	@Override
	protected PacketProcessor<IQ> result() {
		return resultProcessor;
	}

	@Override
	protected PacketProcessor<IQ> error() {
		return null;
	}
}
