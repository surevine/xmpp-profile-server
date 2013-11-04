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

public class Get extends NamespaceProcessorAbstract {

	public static final String MISSING_VCARD_ID = "missing-vcard-id";

	private Element items = null;
	private Element pubsub = null;

	public Get(BlockingQueue<Packet> outQueue, Properties configuration,
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
		pubsub = request.getChildElement();
		if (null != pubsub.element("items")) {
			handleItems();
		} else {
			setErrorCondition(PacketError.Type.cancel,
					PacketError.Condition.feature_not_implemented);
		}
	}

	private void handleItems() throws DataStoreException {
		items = pubsub.element("items");
		if (false == items.attributeValue("node").equals(VCard.NAMESPACE_URI)) {
			setErrorCondition(PacketError.Type.modify,
					PacketError.Condition.bad_request);
			return;
		}
		if (null != items.element("item")) {
			retrieveVcard(items.element("item"));
		} else {
			returnVCards();
		}
	}

	private void returnVCards() {
		// TODO Auto-generated method stub

	}

	private void retrieveVcard(Element item) throws DataStoreException {
		if ((null == item.attributeValue("id"))
				|| (0 == item.attributeValue("id").length())) {
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, MISSING_VCARD_ID);
			return;
		}
		String vcard = dataStore.getVcard(request.getFrom(), item.attributeValue("id"));
		if (null == vcard) {
			setErrorCondition(PacketError.Type.cancel, PacketError.Condition.item_not_found);
			return;
		}

	}
}