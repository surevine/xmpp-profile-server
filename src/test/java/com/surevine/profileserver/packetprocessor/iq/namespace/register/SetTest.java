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
	private IQ registerRequest;
	private IQ unregisterRequest;

	@Before
	public void setUp() throws Exception {
		dataStore = Mockito.mock(DataStore.class);
		queue = new LinkedBlockingQueue<Packet>();
		register = new Set(queue, readConf(), dataStore);
		registerRequest = readStanzaAsIq("/register/registerRequest");
		unregisterRequest = readStanzaAsIq("/register/unregisterRequest");
	}

	@Test
	public void testNonLocalUserCanNotRegister() throws Exception {
		registerRequest.setFrom(new JID("not@from.here"));
		register.process(registerRequest);

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
		Assert.assertEquals(registerRequest.getFrom(), response.getTo());
		Assert.assertEquals(registerRequest.getID(), response.getID());
	}

	@Test
	public void testDataStoreExceptionOnOwnerAddCausesErrorResponsePacket()
			throws Exception {
		Mockito.doThrow(new DataStoreException()).when(dataStore)
				.addOwner(Mockito.any(JID.class));

		register.process(registerRequest);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.wait, error.getType());

		Assert.assertEquals(PacketError.Condition.internal_server_error,
				error.getCondition());
		
		Assert.assertEquals(IQ.Type.error, response.getType());
		Assert.assertEquals(registerRequest.getFrom(), response.getTo());
		Assert.assertEquals(registerRequest.getID(), response.getID());
	}
	
	@Test
	public void testExistingOwnerGetsRegisteredResponse() throws Exception {
		
		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(true);
		register.process(registerRequest);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNull(error);
		Assert.assertNotNull(response.getChildElement().element("registered"));
		
		Assert.assertEquals(IQ.Type.result, response.getType());
		Assert.assertEquals(registerRequest.getFrom(), response.getTo());
		Assert.assertEquals(registerRequest.getID(), response.getID());
	}
	
	@Test 
	public void testResultPacketSentOnSuccess() throws Exception {
		register.process(registerRequest);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNull(error);
		
		Assert.assertEquals(IQ.Type.result, response.getType());
		Assert.assertEquals(registerRequest.getFrom(), response.getTo());
		Assert.assertEquals(registerRequest.getID(), response.getID());
	}
	
	@Test
	public void testCanUnregister() throws Exception {
		
		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(true);
		
		register.process(unregisterRequest);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNull(error);
		
		Assert.assertEquals(IQ.Type.result, response.getType());
		Assert.assertEquals(registerRequest.getFrom(), response.getTo());
		Assert.assertEquals(registerRequest.getID(), response.getID());
	}
	
	@Test
	public void testIfNotRegisteredReceiveRegistrationRequired() throws Exception {
		
		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(false);

		register.process(unregisterRequest);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.auth, error.getType());

		Assert.assertEquals(PacketError.Condition.registration_required,
				error.getCondition());
		
		Assert.assertEquals(IQ.Type.error, response.getType());
		Assert.assertEquals(registerRequest.getFrom(), response.getTo());
		Assert.assertEquals(registerRequest.getID(), response.getID());
	}
	
	@Test
	public void testDataStoreExceptionOnOwnerCheckCausesErrorResponsePacket()
			throws Exception {
		Mockito.doThrow(new DataStoreException()).when(dataStore)
				.hasOwner(Mockito.any(JID.class));

		register.process(unregisterRequest);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.wait, error.getType());

		Assert.assertEquals(PacketError.Condition.internal_server_error,
				error.getCondition());
		
		Assert.assertEquals(IQ.Type.error, response.getType());
		Assert.assertEquals(registerRequest.getFrom(), response.getTo());
		Assert.assertEquals(registerRequest.getID(), response.getID());
	}

}