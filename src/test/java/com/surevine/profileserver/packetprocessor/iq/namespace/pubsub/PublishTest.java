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

public class PublishTest extends IQTestHandler {

	private DataStore dataStore;
	private Set vcard;
	private LinkedBlockingQueue<Packet> queue;

	private IQ request;
	private ArrayList<String> groups;

	@Before
	public void setUp() throws Exception {

		dataStore = Mockito.mock(DataStore.class);
		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(
				true);

		queue = new LinkedBlockingQueue<Packet>();

		vcard = new Set(queue, readConf(), dataStore);

		request = readStanzaAsIq("/pubsub/publish");
	}

	@Test
	public void testMissingNodeAttributeResultsInError() throws Exception {

		IQ modifiedRequest = request;
		modifiedRequest.getChildElement().element("publish").attribute("node")
				.detach();

		vcard.process(request);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals(vcard.MISSING_NODE_ATTRIBUTE,
				error.getApplicationConditionName());
	}

	@Test
	public void testIncorrectValueOnNodeAttributeResultsInError()
			throws Exception {

		IQ modifiedRequest = request;
		modifiedRequest.getChildElement().element("publish").attribute("node")
				.setValue("not-vcard");

		vcard.process(request);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals(vcard.INVALID_NODE_VALUE,
				error.getApplicationConditionName());
	}

	@Test
	public void testMissingItemElementReturnsErrorResponse() throws Exception {

		IQ modifiedRequest = request;
		modifiedRequest.getChildElement().element("publish").element("item")
				.detach();

		vcard.process(request);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
	}

	@Test
	public void testMissingIdAttributeReturnsErrorResponse() throws Exception {

		IQ modifiedRequest = request;
		modifiedRequest.getChildElement().element("publish").element("item")
				.attribute("id").detach();

		vcard.process(request);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals(vcard.MISSING_ID_ATTRIBUTE,
				error.getApplicationConditionName());

	}

	@Test
	public void testEmptyIdAttributeReturnsErrorResponse() throws Exception {

		IQ modifiedRequest = request;
		modifiedRequest.getChildElement().element("publish").element("item")
				.attribute("id").setValue("");

		vcard.process(request);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals(vcard.EMPTY_NAME_ATTRIBUTE,
				error.getApplicationConditionName());

	}
	
	@Test
	public void testInvalidVCardEntryReturnsError() throws Exception {
		IQ modifiedRequest = request;
		modifiedRequest.getChildElement()
		    .element("publish")
		    .element("item")
			.element("vcards")
			.element("vcard")
			.detach();

		vcard.process(request);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals(vcard.INVALID_VCARD_DATA,
				error.getApplicationConditionName());
	}
	
	@Test
	public void testResultResponseReceivedOnSuccess() throws Exception {
		vcard.process(request);
		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNull(error);
		Assert.assertEquals(IQ.Type.result, response.getType());
	}
}