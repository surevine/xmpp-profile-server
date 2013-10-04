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

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.helpers.IQTestHandler;
import com.surevine.profileserver.packetprocessor.iq.namespace.register.Register;

public class SetTest extends IQTestHandler {

	private DataStore dataStore;
	private Set register;
	private LinkedBlockingQueue<Packet> queue;
	private IQ request;

	@Before
	public void setUp() throws Exception {
		dataStore = Mockito.mock(DataStore.class);
		queue = new LinkedBlockingQueue<Packet>();
		register = new Set(queue, readConf(), dataStore);
		request = readStanzaAsIq("/register/request");
	}

	@Test
	public void testNonLocalUserCanNotRegister() throws Exception {
		request.setFrom(new JID("not@from.here"));
		register.process(request);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.cancel, error.getType());

		Assert.assertEquals(PacketError.Condition.not_allowed,
				error.getCondition());
		Assert.assertEquals("not-local-jid",
				error.getApplicationConditionName());
		
		Assert.assertEquals(IQ.Type.error, response.getType());
		Assert.assertEquals(request.getFrom(), response.getTo());
		Assert.assertEquals(request.getID(), response.getID());
	}

	@Test
	public void testDataStoreExceptionCausesErrorResponsePacket()
			throws Exception {
		Mockito.doThrow(new DataStoreException()).when(dataStore)
				.addOwner(Mockito.any(JID.class));

		register.process(request);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.wait, error.getType());

		Assert.assertEquals(PacketError.Condition.internal_server_error,
				error.getCondition());
		
		Assert.assertEquals(IQ.Type.error, response.getType());
		Assert.assertEquals(request.getFrom(), response.getTo());
		Assert.assertEquals(request.getID(), response.getID());
	}
	
	@Test 
	public void testResultPacketSentOnSuccess() throws Exception {
		register.process(request);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNull(error);
		
		Assert.assertEquals(IQ.Type.result, response.getType());
		Assert.assertEquals(request.getFrom(), response.getTo());
		Assert.assertEquals(request.getID(), response.getID());
	}

}