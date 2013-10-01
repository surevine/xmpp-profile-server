package com.surevine.profileserver.packetprocessor.iq.namespace.discoinfo;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;

public class Get implements PacketProcessor<IQ> {

	public static final String ELEMENT_NAME = "query";
	private static final Logger logger = Logger.getLogger(Get.class);
	private final BlockingQueue<Packet> outQueue;
	private final NodeStore nodeStore;
	private String node;
	private IQ result;
	private IQ requestIq;
	private Element query;
	private Map<String, String> conf;

	public Get(BlockingQueue<Packet> outQueue, NodeStore nodeStore) {
		this.outQueue = outQueue;
		this.nodeStore = nodeStore;
	}

	@Override
	public void process(IQ reqIQ) throws Exception {

		requestIq = reqIQ;
		result = IQ.createResultIQ(reqIQ);
		Element elm = reqIQ.getChildElement();
		node = elm.attributeValue("node");
		query = result.setChildElement(ELEMENT_NAME, DiscoInfo.NAMESPACE_URI);

		sendServerDiscoInfo();
	}

	private void sendServerDiscoInfo() throws InterruptedException {

		query.addElement("identity").addAttribute("category", "directory")
				.addAttribute("type", "user profile");

		query.addElement("feature").addAttribute("var",
				"http://jabber.org/protocol/disco#info");
		query.addElement("feature").addAttribute("var",
				"urn:surevine:xmpp:profiles");
		query.addElement("feature").addAttribute("var",
				"urn:ietf:params:xml:ns:vcard-4.0");
logger.info(result.toXML());
		outQueue.put(result);
	}
}