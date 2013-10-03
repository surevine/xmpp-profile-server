package com.surevine.profileserver.packetprocessor.iq.namespace;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;

public abstract class AbstractNamespace implements PacketProcessor<IQ> {

	private static final Logger logger = Logger
			.getLogger(AbstractNamespace.class);

	private BlockingQueue<Packet> outQueue;
	private Properties conf;

	private NodeStore nodeStore;

	public AbstractNamespace(BlockingQueue<Packet> outQueue, Properties conf,
			NodeStore nodeStore) {
		logger.trace("In " + this.getClass().getName());
		this.outQueue = outQueue;
		this.conf = conf;
		this.nodeStore = nodeStore;
	}

	protected abstract PacketProcessor<IQ> get();

	protected abstract PacketProcessor<IQ> set();

	protected abstract PacketProcessor<IQ> result();

	protected abstract PacketProcessor<IQ> error();

	@Override
	public void process(IQ reqIQ) throws Exception {

		PacketProcessor<IQ> processor = null;

		logger.info("Using processor for packet type: " + reqIQ.getType().toString());
		switch (reqIQ.getType()) {
			case get:
				processor = get();
				break;
			case set:
				processor = set();
				break;
			case result:
				processor = result();
				break;
			case error:
				processor = error();
				break;
			default:
				break;
		}

		if (processor != null) {
			processor.process(reqIQ);
			return;
		}

		handleUnexpectedRequest(reqIQ);
	}

	private void handleUnexpectedRequest(IQ reqIQ) throws InterruptedException {
		IQ reply = IQ.createResultIQ(reqIQ);
		reply.setChildElement(reqIQ.getChildElement().createCopy());
		reply.setType(Type.error);
		PacketError pe = new PacketError(
				PacketError.Condition.unexpected_request, PacketError.Type.wait);
		reply.setError(pe);

		outQueue.put(reply);
	}

	protected NodeStore getNodeStore() {
		return nodeStore;
	}

	protected BlockingQueue<Packet> getOutQueue() {
		return outQueue;
	}

	public Properties getConf() {
		return conf;
	}
}