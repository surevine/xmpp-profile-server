package com.surevine.profileserver.queue;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.xmpp.component.ComponentException;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.ProfileEngine;

public class OutQueueConsumer extends QueueConsumer {

	private static final Logger logger = Logger
			.getLogger(OutQueueConsumer.class);
	private final ProfileEngine component;
	private Configuration conf;
	private BlockingQueue<Packet> inQueue;

	public OutQueueConsumer(ProfileEngine component,
			BlockingQueue<Packet> outQueue,
			Configuration conf) {
		super(outQueue);
		this.component = component;
		this.conf = conf;
	}

	@Override
	protected void consume(Packet p) {

		try {
			this.component.sendPacket(p);
		} catch (ComponentException e) {
			logger.error("Sending packet caused error: " + p.toXML(), e);
		}
	}
}