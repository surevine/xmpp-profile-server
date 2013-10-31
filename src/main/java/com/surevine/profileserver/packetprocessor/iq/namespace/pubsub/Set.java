package com.surevine.profileserver.packetprocessor.iq.namespace.pubsub;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.dom4j.Element;
import org.xml.sax.SAXException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.packetprocessor.iq.NamespaceProcessorAbstract;
import com.surevine.profileserver.packetprocessor.iq.namespace.vcard.VCard;

import ezvcard.Ezvcard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarnings;
import ezvcard.io.VCardWriter;

public class Set extends NamespaceProcessorAbstract {

	public static final String MISSING_NODE_ATTRIBUTE = "missing-node-attribute";
	public static final String INVALID_NODE_VALUE = "invalid-node-value";
	
	private String name = null;
	private ezvcard.VCard vcard = null;

	public Set(BlockingQueue<Packet> outQueue, Properties configuration,
			DataStore dataStore) {
		super(outQueue, configuration, dataStore);
	}

	@Override
	public void process(IQ packet) throws Exception {
		request = packet;
		response = IQ.createResultIQ(packet);

		if (false == dataStore.hasOwner(request.getFrom())) {
			setErrorCondition(PacketError.Type.auth,
					PacketError.Condition.registration_required);
		} else {
			Element pubsub = request.getChildElement();
			if (null != pubsub.element("publish")) {
				handlePublish();
			} else if (null != pubsub.element("retract")) {
				handleDelete();
			} else {
				setErrorCondition(PacketError.Type.cancel,
						PacketError.Condition.feature_not_implemented);
			}
		}
		outQueue.put(response);
	}

	private void handleDelete() {
		// TODO Auto-generated method stub
		
	}

	private void handlePublish() {
		Element publish = request.getChildElement().element("publish");
		
		String node = publish.attributeValue("node");
		if (null == node) {
			createExtendedErrorReply(PacketError.Type.modify, PacketError.Condition.bad_request, MISSING_NODE_ATTRIBUTE);
			return;
		} else if (VCard.NAMESPACE_URI != node) {
			createExtendedErrorReply(PacketError.Type.modify, PacketError.Condition.bad_request, INVALID_NODE_VALUE);
			return;
		}
		name = request.getChildElement().attributeValue("id");

		if ((null == name) || (0 == name.length())) {
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, "id-required");
		} else {
			try {
				parseVCard();
			} catch (DataStoreException e) {
				logger.error(e);
				setErrorCondition(PacketError.Type.wait,
						PacketError.Condition.internal_server_error);
			} catch (SAXException e) {
				logger.error(e);
				setErrorCondition(PacketError.Type.modify,
						PacketError.Condition.bad_request);
			}
		}
	}

	private void parseVCard() throws SAXException, DataStoreException {

		ezvcard.VCard vcard = Ezvcard.parseXml(
				request.getChildElement().asXML()).first();
		dataStore.saveVcard(request.getFrom(), name, vcard.writeXml());
	}

}