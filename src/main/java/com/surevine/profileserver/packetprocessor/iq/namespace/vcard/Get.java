package com.surevine.profileserver.packetprocessor.iq.namespace.vcard;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.packetprocessor.iq.NamespaceProcessorAbstract;

public class Get extends NamespaceProcessorAbstract {

	private Element vcard;
	
	public Get(BlockingQueue<Packet> outQueue, Properties configuration,
			NodeStore nodeStore) {
		super(outQueue, configuration, nodeStore);
	}

	@Override
	public void process(IQ packet) throws Exception {
        request  = packet;
		response = IQ.createResultIQ(packet);
		vcard = packet.getElement().element("vcard");

		if (null != vcard.attributeValue("jid")) {
			handleJidRequest();
		} else if (null != vcard.attributeValue("id")) {
			handleIdRequest();
		} else {
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, "jid-or-id-required");
		}
		outQueue.put(response);
	}

	private void handleIdRequest() {
		// TODO Auto-generated method stub
		
	}

	private void handleJidRequest() {
		JID user = new JID(vcard.attributeValue("jid"));
		if (false == nodeStore.hasOwner(user)) {
			setErrorCondition(PacketError.Type.cancel, PacketError.Condition.item_not_found);
			return;
		}
		
	}

}
