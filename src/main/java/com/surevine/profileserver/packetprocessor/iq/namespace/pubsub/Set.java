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
	public static final String MISSING_ID_ATTRIBUTE = "missing-id-attribute";
	public static final String EMPTY_NAME_ATTRIBUTE = "empty-name-attribute";
	public static final String INVALID_VCARD_DATA = "invalid-vcard4-data";

	private String name = null;
	private ezvcard.VCard vcard = null;
	
	private Element item = null;

	public Set(BlockingQueue<Packet> outQueue, Properties configuration,
			DataStore dataStore) {
		super(outQueue, configuration, dataStore);
	}

	@Override
	public void process(IQ packet) throws Exception {
		request = packet;
		response = IQ.createResultIQ(packet);

		try {
			processRequest();
		} catch (DataStoreException e) {
			logger.error(e);
			setErrorCondition(PacketError.Type.wait,
					PacketError.Condition.internal_server_error);
		}

		outQueue.put(response);
	}

	private void processRequest() throws DataStoreException {
		if (false == dataStore.hasOwner(request.getFrom())) {
			setErrorCondition(PacketError.Type.auth,
					PacketError.Condition.registration_required);
			return;
		}
		Element pubsub = request.getChildElement();
		if (null != pubsub.element("publish")) {
			handlePublish();
		} else if (null != pubsub.element("retract")) {
			handleRetract();
		} else {
			setErrorCondition(PacketError.Type.cancel,
					PacketError.Condition.feature_not_implemented);
		}
	}

	private void handleRetract() {
		Element retract = request.getChildElement().element("retract");

        if (false == stanzaCheck(retract)) return;

	}

	private boolean stanzaCheck(Element element) {
		String node = element.attributeValue("node");

		if (null == node) {
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, MISSING_NODE_ATTRIBUTE);
			return false;
		} else if (false == VCard.NAMESPACE_URI.equals(node)) {
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, INVALID_NODE_VALUE);
			return false;
		}

		item = element.element("item");
		if (null == item) {
			setErrorCondition(PacketError.Type.modify,
					PacketError.Condition.bad_request);
			return false;
		}

		name = item.attributeValue("id");
		if (null == name) {
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, MISSING_ID_ATTRIBUTE);
			return false;
		} else if (0 == name.length()) {
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, EMPTY_NAME_ATTRIBUTE);
			return false;
		}
		return true;
	}

	private void handlePublish() {
		Element publish = request.getChildElement().element("publish");
        if (false == stanzaCheck(publish)) return;
        
		try {
			parseVCard(item.element("vcards"));
		} catch (DataStoreException e) {
			logger.error(e);
			setErrorCondition(PacketError.Type.wait,
					PacketError.Condition.internal_server_error);
		} catch (SAXException e) {
			logger.error(e);
			setErrorCondition(PacketError.Type.modify,
					PacketError.Condition.bad_request);
		} catch (NullPointerException e) {
			logger.error(e);
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, INVALID_VCARD_DATA);
		}
	}

	private void parseVCard(Element vcardElement) throws SAXException,
			DataStoreException, NullPointerException {
        ezvcard.VCard vcard = Ezvcard.parseXml(vcardElement.asXML()).first();
		ValidationWarnings warnings = vcard.validate(VCardVersion.V4_0);
		if (0 != warnings.getWarnings().size()) {
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, INVALID_VCARD_DATA);
			return;
		}
		saveVcard(vcard);
	}

	private void saveVcard(ezvcard.VCard vcard) throws DataStoreException {
		dataStore.saveVcard(request.getFrom(), name, vcard.writeXml());
	}

}