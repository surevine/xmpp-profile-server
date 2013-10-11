package com.surevine.profileserver.packetprocessor.iq.namespace.vcard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.dom4j.Element;
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

public class GetTest extends IQTestHandler {

	private DataStore dataStore;
	private Get vcard;
	private LinkedBlockingQueue<Packet> queue;

	private IQ request;

	@Before
	public void setUp() throws Exception {
		dataStore = Mockito.mock(DataStore.class);
		queue = new LinkedBlockingQueue<Packet>();

		vcard = new Get(queue, readConf(), dataStore);

		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(
				true);
	}

	@Test
	public void testNoIdOrJidReturnsErrorResult() throws Exception {

		vcard.process(readStanzaAsIq("/vcard/get-missing-attributes"));

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals("jid-required", error.getApplicationConditionName());
	}

	@Test
	public void testNonExistingUserReturnsItemNotFound() throws Exception {

		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(
				false);

		vcard.process(readStanzaAsIq("/vcard/get-jid-attribute"));

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.cancel, error.getType());

		Assert.assertEquals(PacketError.Condition.item_not_found,
				error.getCondition());
	}

	@Test
	public void testDataStoreExceptionReturnsInternalServerError()
			throws Exception {

		Mockito.doThrow(new DataStoreException()).when(dataStore)
				.hasOwner(Mockito.any(JID.class));

		vcard.process(readStanzaAsIq("/vcard/get-jid-attribute"));

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.wait, error.getType());

		Assert.assertEquals(PacketError.Condition.internal_server_error,
				error.getCondition());
	}

	@Test
	public void testUserNotInRosterReceivesPublicVcard() throws Exception {

		Mockito.when(
				dataStore.getRosterGroupsForUser(Mockito.any(JID.class),
						Mockito.any(JID.class))).thenReturn(new ArrayList<String>());
		Mockito.when(dataStore.getPublicVcard(Mockito.any(JID.class))).thenReturn(null);
		vcard.process(readStanzaAsIq("/vcard/get-jid-attribute"));

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNull(error);

	}

	@Test
	public void testNoPublicVcardResultsInEmptyResponse() throws Exception {

	}

	@Test
	public void testUserInRosterGroupWithNoMapReceivesPublicVcard()
			throws Exception {

	}

	@Test
	public void testUserInRosterGroupWithMapReceivesThatVcard()
			throws Exception {

	}
	
	@Test 
	public void testUserGetsHighestPriorityVcard() throws Exception {
		
	}

}