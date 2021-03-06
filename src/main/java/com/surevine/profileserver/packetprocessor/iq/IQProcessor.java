package com.surevine.profileserver.packetprocessor.iq;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.namespace.command.Command;
import com.surevine.profileserver.packetprocessor.iq.namespace.discoinfo.DiscoInfo;
import com.surevine.profileserver.packetprocessor.iq.namespace.pubsub.PubSub;
import com.surevine.profileserver.packetprocessor.iq.namespace.register.Register;
import com.surevine.profileserver.packetprocessor.iq.namespace.surevine.Surevine;
import com.surevine.profileserver.packetprocessor.iq.namespace.vcard.VCard;

public class IQProcessor implements PacketProcessor<IQ> {

	private static final Logger logger = Logger.getLogger(IQProcessor.class);

	private Map<String, PacketProcessor<IQ>> processorsPerNamespace = new HashMap<String, PacketProcessor<IQ>>();
	private BlockingQueue<Packet> outQueue;

	public IQProcessor(BlockingQueue<Packet> outQueue,
			Configuration configuration, DataStore dataStore) {
		this.outQueue = outQueue;

		PubSub pubsub = new PubSub(
				outQueue, configuration, dataStore);
		
		processorsPerNamespace.put(VCard.NAMESPACE_URI, new VCard(outQueue,
				configuration, dataStore));
		processorsPerNamespace.put(DiscoInfo.NAMESPACE_URI, new DiscoInfo(
				outQueue, configuration, dataStore));
		processorsPerNamespace.put(Register.NAMESPACE_URI, new Register(
				outQueue, configuration, dataStore));
		processorsPerNamespace.put(Surevine.NAMESPACE_URI, new Surevine(
				outQueue, configuration, dataStore));
		processorsPerNamespace.put(Command.NAMESPACE_URI, new Command(
				outQueue, configuration, dataStore));
		processorsPerNamespace.put(PubSub.NAMESPACE_URI, pubsub);
		processorsPerNamespace.put(PubSub.NAMESPACE_OWNER, pubsub);
	}

	@Override
	public void process(IQ packet) throws Exception {

		try {
			processPacket(packet);
		} catch (Exception e) {
			if (true == packet.getType().toString().equals("result"))
				return;
			IQ reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			reply.setType(Type.error);
			PacketError pe = new PacketError(
					org.xmpp.packet.PacketError.Condition.internal_server_error,
					org.xmpp.packet.PacketError.Type.wait);
			reply.setError(pe);
			logger.error("Error while processing packet.", e);
			e.printStackTrace();
			this.outQueue.put(reply);
		}
	}

	private void processPacket(IQ packet) throws Exception,
			InterruptedException {
		if (null != packet.getChildElement()) {
			logger.debug("Finding IQ processor for namespace "
					+ packet.getChildElement().getNamespaceURI());

			PacketProcessor<IQ> namespaceProcessor = processorsPerNamespace
					.get(packet.getChildElement().getNamespaceURI());

			if (packet.getChildElement().getNamespaceURI() != null
					&& namespaceProcessor != null) {
				logger.trace("Using namespace processor: "
						+ namespaceProcessor.getClass().getName());
				namespaceProcessor.process(packet);
				return;

			}
		}

		logger.debug("Couldn't find processor for packet");

		if ((packet.getType() == IQ.Type.set)
				|| (packet.getType() == IQ.Type.get)) {

			IQ reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			reply.setType(Type.error);
			PacketError pe = new PacketError(
					org.xmpp.packet.PacketError.Condition.service_unavailable,
					org.xmpp.packet.PacketError.Type.cancel);
			reply.setError(pe);

			this.outQueue.put(reply);
			return;

		}
		logger.error("Could not handle packet " + packet.toXML());
	}
}