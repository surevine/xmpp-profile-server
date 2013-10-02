package com.surevine.profileserver.packetprocessor.iq.namespace.discoinfo;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.helpers.IQTestHandler;

public class GetTest extends IQTestHandler {

	private NodeStore nodeStore;
	private Get discoInfo;
	private LinkedBlockingQueue<Packet> queue;

	private IQ request;

	@Before
	public void setUp() throws Exception {
		nodeStore = Mockito.mock(NodeStore.class);
		queue = new LinkedBlockingQueue<Packet>();

		discoInfo = new Get(queue, nodeStore);
		request = readStanzaAsIq("/disco-info/get");
	}

	@Test
	public void testReturnsQueryElement() throws Exception {
		
		discoInfo.process(request);

		Assert.assertEquals(1, queue.size());
		IQ result = (IQ) queue.poll();

		Element iq = result.getElement();
		Assert.assertEquals("iq", iq.getName());
		Assert.assertEquals(IQ.Type.result, result.getType());
		Assert.assertEquals("info3", result.getID());

		Element query = iq.element("query");
		Assert.assertNotNull(query);
		Assert.assertEquals(DiscoInfo.NAMESPACE_URI, query
				.getNamespaceForPrefix("").getText());
	}

	@Test
	public void testReturnsExceptedIdentity() throws Exception {
		
		discoInfo.process(request);

		Assert.assertEquals(1, queue.size());
		IQ result = (IQ) queue.poll();
		Element query = result.getElement().element("query");
		
		Assert.assertEquals(1, query.elements("identity").size());

		Element identity = query.element("identity");
		Assert.assertEquals("directory", identity.attributeValue("category"));
		Assert.assertEquals("user profile", identity.attributeValue("type"));
	}

	@Test
	public void testReturnsExceptedFeatures() throws Exception {
		
		discoInfo.process(request);

		Assert.assertEquals(1, queue.size());
		IQ result = (IQ) queue.poll();
		Element query = result.getElement().element("query");
		
		Assert.assertEquals(3, query.elements("feature").size());
		List<Element> features = query.elements("feature");
		Assert.assertEquals(DiscoInfo.NAMESPACE_URI, features.get(0)
				.attributeValue("var"));
		Assert.assertEquals(Configuration.NS_SUREVINE, features.get(1)
				.attributeValue("var"));
		Assert.assertEquals("urn:ietf:params:xml:ns:vcard-4.0", features.get(2)
				.attributeValue("var"));

	}
}