package com.surevine.profileserver.queue;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.DataStoreFactory;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.packetprocessor.iq.IQProcessor;

public class InQueueConsumer extends QueueConsumer {

	private static final Logger logger = Logger
			.getLogger(InQueueConsumer.class);

	private final BlockingQueue<Packet> outQueue;
	private final Configuration conf;
	private final DataStoreFactory dataStoreFactory;

	public InQueueConsumer(BlockingQueue<Packet> outQueue, Configuration conf,
			BlockingQueue<Packet> inQueue,
			DataStoreFactory dataStoreFactory) {
		super(inQueue);
		this.outQueue = outQueue;
		this.conf = conf;
		this.dataStoreFactory = dataStoreFactory;
	}

	@Override
	protected void consume(Packet p) {
		DataStore dataStore = null;
		try {
			Long start = System.currentTimeMillis();

			String xml = p.toXML();
			logger.debug("Received payload: '" + xml + "'.");
			
			dataStore = dataStoreFactory.create();
			
			if (p instanceof IQ) {
				new IQProcessor(outQueue, conf, dataStore).process((IQ) p);
			}

			logger.debug("Payload handled in '"
					+ Long.toString((System.currentTimeMillis() - start))
					+ "' milliseconds.");

		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage(), e);
		} finally {
			try {
				dataStore.close();
			} catch (DataStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}