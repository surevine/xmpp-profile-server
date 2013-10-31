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

public class SetTest extends IQTestHandler {

	private DataStore dataStore;
	private Set vcard;
	private LinkedBlockingQueue<Packet> queue;

	private IQ publishRequest;
	private ArrayList<String> groups;

	@Before
	public void setUp() throws Exception {
		dataStore = Mockito.mock(DataStore.class);
		queue = new LinkedBlockingQueue<Packet>();

		vcard = new Set(queue, readConf(), dataStore);

		publishRequest = readStanzaAsIq("/pubsub/publish");
	}

	@Test
	public void testUnknownOwnerRequestedToRegister() throws Exception {

		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(false);
		
		vcard.process(publishRequest);

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

		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(true);
		
		IQ modifiedRequest = publishRequest;
		modifiedRequest.getChildElement()
		    .element("publish")
			.detach();

		vcard.process(publishRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.cancel, error.getType());

		Assert.assertEquals(PacketError.Condition.feature_not_implemented,
				error.getCondition());
	}
}