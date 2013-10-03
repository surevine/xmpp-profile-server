package com.surevine.profileserver.packetprocessor.iq.namespace.register;

import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.apache.bcel.generic.GETSTATIC;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.db.exception.NodeStoreException;
import com.surevine.profileserver.helpers.IQTestHandler;
import com.surevine.profileserver.packetprocessor.iq.namespace.register.Register;

public class GetTest extends IQTestHandler {

	private NodeStore nodeStore;
	private Get register;
	private LinkedBlockingQueue<Packet> queue;
	private IQ request;

	@Before
	public void setUp() throws Exception {
		nodeStore = Mockito.mock(NodeStore.class);
		queue = new LinkedBlockingQueue<Packet>();
		register = new Get(queue, readConf(), nodeStore);
		request = readStanzaAsIq("/register/request");
	}

	@Test
	public void testNonLocalUserCanNotRegister() throws Exception {
		request.setFrom(new JID("not@from.here"));
		register.process(request);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		Assert.assertEquals(IQ.Type.error, response.getType());
		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.cancel, error.getType());

		Assert.assertEquals(PacketError.Condition.not_allowed,
				error.getCondition());
		Assert.assertEquals("not-local-jid",
				error.getApplicationConditionName());
	}

	@Test
	public void testDataStoreExceptionCausesErrorResponsePacket()
			throws Exception {
		Mockito.doThrow(new NodeStoreException()).when(nodeStore)
				.addOwner(Mockito.any(JID.class));

		register.process(request);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		Assert.assertEquals(IQ.Type.error, response.getType());
		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.wait, error.getType());

		Assert.assertEquals(PacketError.Condition.internal_server_error,
				error.getCondition());
	}

}