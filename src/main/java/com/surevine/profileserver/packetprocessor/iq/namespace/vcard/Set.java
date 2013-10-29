package com.surevine.profileserver.packetprocessor.iq.namespace.vcard;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.packetprocessor.iq.NamespaceProcessorAbstract;

public class Set extends NamespaceProcessorAbstract {

	public Set(BlockingQueue<Packet> outQueue, Properties configuration,
			DataStore dataStore) {
		super(outQueue, configuration, dataStore);
	}

	@Override
	public void process(IQ packet) throws Exception {
        request  = packet;
		response = IQ.createResultIQ(packet);

		setErrorCondition(PacketError.Type.cancel, PacketError.Condition.feature_not_implemented);
		outQueue.put(response);
	}

}