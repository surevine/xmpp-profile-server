package com.surevine.profileserver.packetprocessor.iq.namespace.vcard;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.packetprocessor.iq.NamespaceProcessorAbstract;

public class Get extends NamespaceProcessorAbstract {

	private Element vcard;
	private JID owner;
	
	public Get(BlockingQueue<Packet> outQueue, Properties configuration,
			DataStore dataStore) {
		super(outQueue, configuration, dataStore);
	}

	@Override
	public void process(IQ packet) throws Exception {
        request  = packet;
		response = IQ.createResultIQ(packet);
		vcard = packet.getElement().element("vcard");

		try {
			if (null != vcard.attributeValue("jid")) {
				handleJidRequest();
			} else {
				createExtendedErrorReply(PacketError.Type.modify,
						PacketError.Condition.bad_request, "jid-required");
			}
		} catch (DataStoreException e) {
			logger.error(e);
			setErrorCondition(PacketError.Type.wait, PacketError.Condition.internal_server_error);
		} catch (DocumentException e) {
			logger.error(e);
			setErrorCondition(PacketError.Type.wait, PacketError.Condition.internal_server_error);
		}
		outQueue.put(response);
	}
	
	private void handleJidRequest() throws Exception {
		owner = new JID(vcard.attributeValue("jid"));
		if (false == dataStore.hasOwner(owner)) {
			setErrorCondition(PacketError.Type.cancel, PacketError.Condition.item_not_found);
			return;
		}
		String vcard = dataStore.getVcardForUser(owner, request.getFrom());
		if (null == vcard) {
			sendPublicVcard();
		} else {
			sendVcard(vcard);
		}
	}

	private void sendPublicVcard() throws Exception {
		String vcardString = dataStore.getPublicVcard(owner);
		sendVcard(vcardString);
	}
	
	private void sendVcard(String vcardString) throws Exception {
		Element vcardElement = response.getElement().addElement("vcard", VCard.NAMESPACE_URI);
		if (null != vcardString) {
			List<Element> vcardParts = parseXml(vcardString).elements();
			for (Element vcardPart : vcardParts) {
				vcardPart.detach();
			    vcardElement.add(vcardPart);
			}
		}
	}

}
