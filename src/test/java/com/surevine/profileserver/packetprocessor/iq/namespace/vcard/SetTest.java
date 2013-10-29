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

public class SetTest extends IQTestHandler {

	private DataStore dataStore;
	private Set vcard;
	private LinkedBlockingQueue<Packet> queue;

	private IQ request;
	private ArrayList<String> groups;

	@Before
	public void setUp() throws Exception {
		dataStore = Mockito.mock(DataStore.class);
		queue = new LinkedBlockingQueue<Packet>();

		vcard = new Set(queue, readConf(), dataStore);
		
		request = readStanzaAsIq("/vcard/set");
	}
	
	@Test
	public void testMissingNameAttributeReturnsErrorResponse() throws Exception {
		
		IQ modifiedRequest = request;
		modifiedRequest.getChildElement().attribute("name").detach();

		vcard.process(request);
		
		IQ response = (IQ) queue.poll();
		
		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals("name-required", error.getApplicationConditionName());
				
	}
	
	@Test
	public void testEmptyNameAttributeReturnsErrorResponse() throws Exception {
		
		IQ modifiedRequest = request;
		modifiedRequest.getChildElement().attribute("name").setValue("");

		vcard.process(request);
		
		IQ response = (IQ) queue.poll();
		
		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals("name-required", error.getApplicationConditionName());
				
	}
	
	@Test
	public void testUserWhoIsntInSystemReceivesErrorResponse() throws Exception {
		
		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(false);
		
		vcard.process(request);
		
		IQ response = (IQ) queue.poll();
		
		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.auth, error.getType());

		Assert.assertEquals(PacketError.Condition.registration_required,
				error.getCondition());
		
	}

}