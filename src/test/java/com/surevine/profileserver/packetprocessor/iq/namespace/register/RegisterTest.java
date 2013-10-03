package com.surevine.profileserver.packetprocessor.iq.namespace.register;

import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.helpers.IQTestHandler;
import com.surevine.profileserver.packetprocessor.iq.namespace.register.Register;

public class RegisterTest extends IQTestHandler {

	private NodeStore nodeStore;
	private Register register;
	private LinkedBlockingQueue<Packet> queue;

	private IQ request;

	@Before
	public void setUp() throws Exception {
		nodeStore = Mockito.mock(NodeStore.class);
		queue = new LinkedBlockingQueue<Packet>();

		register = new Register(queue, readConf(), nodeStore);
	}

	@Test
	public void testVoid() throws Exception {

	}

}