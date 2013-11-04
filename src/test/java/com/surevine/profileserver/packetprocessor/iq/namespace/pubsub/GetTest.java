package com.surevine.profileserver.packetprocessor.iq.namespace.pubsub;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

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
import com.surevine.profileserver.packetprocessor.iq.namespace.vcard.VCard;

public class GetTest extends IQTestHandler {

	private DataStore dataStore;
	private Get vcard;
	private LinkedBlockingQueue<Packet> queue;

	private IQ itemsRequest;
	private IQ itemRequest;
	private ArrayList<String> groups;

	@Before
	public void setUp() throws Exception {
		dataStore = Mockito.mock(DataStore.class);
		queue = new LinkedBlockingQueue<Packet>();

		vcard = new Get(queue, readConf(), dataStore);

		itemsRequest = readStanzaAsIq("/pubsub/items");
		itemRequest = readStanzaAsIq("/pubsub/item");

		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(
				true);
	}

	@Test
	public void testDataStoreExceptionReturnsExpectedError() throws Exception {

		Mockito.doThrow(new DataStoreException()).when(dataStore)
				.hasOwner(Mockito.any(JID.class));

		vcard.process(itemsRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.wait, error.getType());

		Assert.assertEquals(PacketError.Condition.internal_server_error,
				error.getCondition());
	}

	@Test
	public void testUnknownOwnerRequestedToRegister() throws Exception {

		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(
				false);

		vcard.process(itemsRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.auth, error.getType());

		Assert.assertEquals(PacketError.Condition.registration_required,
				error.getCondition());

	}

	@Test
	public void testUnknownChildElementResultsInErrorResponse()
			throws Exception {

		IQ modifiedRequest = itemsRequest.createCopy();
		modifiedRequest.getChildElement().element("items").detach();

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.cancel, error.getType());

		Assert.assertEquals(PacketError.Condition.feature_not_implemented,
				error.getCondition());
	}

	@Test
	public void testReceiveErrorIfNodeIsNotEqualToVCardNamespace()
			throws Exception {
		IQ modifiedRequest = itemsRequest.createCopy();
		modifiedRequest.getChildElement().element("items").attribute("node")
				.setValue("not-vcard-namespace");

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
	}

	@Test
	public void testRequestingSingleVCardWithNoIdReturnsError()
			throws Exception {
		IQ modifiedRequest = itemRequest.createCopy();
		modifiedRequest.getChildElement().element("items").element("item")
				.attribute("id").detach();

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());

		Assert.assertEquals(vcard.MISSING_VCARD_ID,
				error.getApplicationConditionName());
	}

	@Test
	public void testRequestingSingleVCardWithEmptyIdReturnsError()
			throws Exception {
		IQ modifiedRequest = itemRequest.createCopy();
		modifiedRequest.getChildElement().element("items").element("item")
				.attribute("id").setValue("");

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals(vcard.MISSING_VCARD_ID,
				error.getApplicationConditionName());
	}

	@Test
	public void testRequestingVCardWhichDoesntExistReturnsItemNotFound()
			throws Exception {

		Mockito.when(
				dataStore.getVcard(Mockito.any(JID.class), Mockito.anyString()))
				.thenReturn(null);

		vcard.process(itemRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.cancel, error.getType());

		Assert.assertEquals(PacketError.Condition.item_not_found,
				error.getCondition());
	}

	@Test
	public void testRequestingSingleVCardReturnsExpectedData() throws Exception {
		// Check for 'default'
	}

	@Test
	public void testCanRetrieveVCardsWhereThereAreNone() throws Exception {

		/*
		 * vcard.process(itemsRequest);
		 * 
		 * IQ response = (IQ) queue.poll();
		 * 
		 * Assert.assertEquals(IQ.Type.result, response.getType()); Element
		 * items = response.getElement().element("pubsub",
		 * PubSub.NAMESPACE_URI).element("items", VCard.NAMESPACE_URI);
		 * Assert.assertNotNull(items); Assert.assertEquals(0,
		 * items.children().size());
		 */
	}
}