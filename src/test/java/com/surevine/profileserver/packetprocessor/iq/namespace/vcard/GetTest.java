package com.surevine.profileserver.packetprocessor.iq.namespace.vcard;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.helpers.IQTestHandler;

public class GetTest extends IQTestHandler {

	private NodeStore nodeStore;
	private Get vcard;
	private LinkedBlockingQueue<Packet> queue;

	private IQ request;

	@Before
	public void setUp() throws Exception {
		nodeStore = Mockito.mock(NodeStore.class);
		queue = new LinkedBlockingQueue<Packet>();

		vcard = new Get(queue, readConf(), nodeStore);
	}

	@Test
	public void testStub() throws Exception {
		request = readStanzaAsIq("/vcard/get-missing-attributes");
		vcard.process(request);

		
	}

}