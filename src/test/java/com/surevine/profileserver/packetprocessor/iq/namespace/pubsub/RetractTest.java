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

public class RetractTest extends IQTestHandler {

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

		request = readStanzaAsIq("/pubsub/retract");
	}

	@Test
	public void testMissingNodeAttributeResultsInError() throws Exception {

		IQ modifiedRequest = request;
		modifiedRequest.getChildElement().element("retract").attribute("node")
				.detach();

		vcard.process(modifiedRequest);

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
		modifiedRequest.getChildElement().element("retract").attribute("node")
				.setValue("not-vcard");

		vcard.process(modifiedRequest);

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
		modifiedRequest.getChildElement().element("retract").element("item")
				.detach();

		vcard.process(modifiedRequest);

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
		modifiedRequest.getChildElement().element("retract").element("item")
				.attribute("id").detach();

		vcard.process(modifiedRequest);

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
		modifiedRequest.getChildElement().element("retract").element("item")
				.attribute("id").setValue("");

		vcard.process(modifiedRequest);

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
	public void testCantDeleteVCardThatIsInUse() throws Exception {

		ArrayList<String> groups = new ArrayList<String>();
		groups.add("family");
		Mockito.when(
				dataStore.getRosterGroupsForVCard(Mockito.any(JID.class),
						Mockito.anyString())).thenReturn(groups);
		
		vcard.process(request);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		System.out.println("\n\n" + response.toXML());
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.not_acceptable,
				error.getCondition());
		Assert.assertEquals(vcard.VCARD_USED_IN_ROSTERMAP,
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