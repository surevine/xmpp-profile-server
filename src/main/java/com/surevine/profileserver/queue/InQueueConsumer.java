package com.surevine.profileserver.queue;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.db.NodeStoreFactory;
import com.surevine.profileserver.db.exception.NodeStoreException;
import com.surevine.profileserver.packetprocessor.iq.IQProcessor;

public class InQueueConsumer extends QueueConsumer {

	private static final Logger logger = Logger
			.getLogger(InQueueConsumer.class);

	private final BlockingQueue<Packet> outQueue;
	private final Configuration conf;
	private final NodeStoreFactory nodeStoreFactory;

	public InQueueConsumer(BlockingQueue<Packet> outQueue, Configuration conf,
			BlockingQueue<Packet> inQueue,
			NodeStoreFactory nodeStoreFactory) {
		super(inQueue);
		this.outQueue = outQueue;
		this.conf = conf;
		this.nodeStoreFactory = nodeStoreFactory;
	}

	@Override
	protected void consume(Packet p) {
		NodeStore nodeStore = null;
		try {
			Long start = System.currentTimeMillis();

			String xml = p.toXML();
			logger.debug("Received payload: '" + xml + "'.");
			
			nodeStore = nodeStoreFactory.create();
			
			if (p instanceof IQ) {
				new IQProcessor(outQueue, conf, nodeStore).process((IQ) p);
			}

			logger.debug("Payload handled in '"
					+ Long.toString((System.currentTimeMillis() - start))
					+ "' milliseconds.");

		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage(), e);
		} finally {
			try {
				nodeStore.close();
			} catch (NodeStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}