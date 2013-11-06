package com.surevine.profileserver.packetprocessor.iq.namespace.pubsub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.xml.sax.SAXException;
import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.model.VCardMeta;
import com.surevine.profileserver.packetprocessor.iq.NamespaceProcessorAbstract;
import com.surevine.profileserver.packetprocessor.iq.namespace.surevine.Surevine;
import com.surevine.profileserver.packetprocessor.iq.namespace.vcard.VCard;

import ezvcard.Ezvcard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarnings;
import ezvcard.io.VCardWriter;

public class Get extends NamespaceProcessorAbstract {

	public static final String MISSING_VCARD_ID = "missing-vcard-id";

	public static final String VCARD_DEFAULT = "default-vcard";
	public static final String VCARD_DEFAULT_LABEL = "vCard to display to users with no roster group";;

	public static final String DATAFORM_VARIABLE_PREFIX = "vcard#";

	public static final String DATAFORM_LABEL = "Roster groups which can view vCard '%s'";

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
		} else if (null != pubsub.element("configure")) {
			handleConfiguration();
		} else {
			setErrorCondition(PacketError.Type.cancel,
					PacketError.Condition.feature_not_implemented);
		}
	}

	private void handleConfiguration() throws DataStoreException {
		Element configure = request.getChildElement().element("configure");
		if ((null == configure.attributeValue("node"))
				|| (false == configure.attributeValue("node").equals(
						VCard.NAMESPACE_URI))) {
			setErrorCondition(PacketError.Type.modify,
					PacketError.Condition.bad_request);
			return;
		}
		buildConfigurationResponse();
	}

	private void buildConfigurationResponse() throws DataStoreException {

		List<VCardMeta> vcards = dataStore.getVCardList(request.getFrom());
		List<String> rosterGroups = dataStore.getOwnerRosterGroupList(request
				.getFrom());

		Element configure = response.getElement()
				.addElement("pubsub", PubSub.NAMESPACE_OWNER)
				.addElement("configure");
		configure.addAttribute("node", VCard.NAMESPACE_URI);

		DataForm dataForm = new DataForm(DataForm.Type.result);
		for (VCardMeta vcard : vcards) {
			addVCardField(dataForm, vcard, rosterGroups);
		}
		addDefaultField(dataForm, vcards);
		configure.add(dataForm.getElement());
	}

	private void addDefaultField(DataForm dataForm, List<VCardMeta> vcards) {
		FormField defaultField = dataForm.addField(VCARD_DEFAULT,
				VCARD_DEFAULT_LABEL, FormField.Type.list_single);
		defaultField.addOption("None", VCard.NONE);
		for (VCardMeta vcard : vcards) {
			defaultField.addOption(vcard.getName(), vcard.getName());
			if (true == vcard.isDefault())
				defaultField.addValue(vcard.getName());
		}
	}

	private void addVCardField(DataForm dataForm, VCardMeta vcard,
			List<String> rosterGroups) {
		FormField field = dataForm.addField(DATAFORM_VARIABLE_PREFIX + vcard.getName(),
				String.format(DATAFORM_LABEL, vcard.getName()),
				FormField.Type.list_multi);
		for (String group : rosterGroups) {
			field.addOption(group, group);
		}
		// Add values
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

	private void returnVCards() throws DataStoreException {
		List<VCardMeta> vcards = dataStore.getVCardList(request.getFrom());
		Element responseItems = response.getElement()
				.addElement("pubsub", PubSub.NAMESPACE_URI).addElement("items");
		responseItems.addAttribute("node", VCard.NAMESPACE_URI);

		for (VCardMeta vcard : vcards) {
			Element item = responseItems.addElement("item");
			item.addAttribute("id", vcard.getName());
		}
	}

	private void retrieveVcard(Element item) throws DataStoreException {
		if ((null == item.attributeValue("id"))
				|| (0 == item.attributeValue("id").length())) {
			createExtendedErrorReply(PacketError.Type.modify,
					PacketError.Condition.bad_request, MISSING_VCARD_ID);
			return;
		}
		String vcard = dataStore.getVcard(request.getFrom(),
				item.attributeValue("id"));
		if (null == vcard) {
			setErrorCondition(PacketError.Type.cancel,
					PacketError.Condition.item_not_found);
			return;
		}
		sendVcard(vcard, item.attributeValue("id"));
	}

	private void sendVcard(String vcardString, String name)
			throws DataStoreException {
		Element responseItems = response.getElement()
				.addElement("pubsub", PubSub.NAMESPACE_URI).addElement("items");
		responseItems.addAttribute("node", VCard.NAMESPACE_URI);

		Element responseItem = responseItems.addElement("item");

		responseItem.addNamespace("profile", Surevine.NAMESPACE_URI);
		responseItem.addAttribute("profile:default",
				dataStore.getVCardMeta(request.getFrom(), name)
						.defaultAttribute());

		responseItem.addAttribute("id", name);
		try {
			Element vcard = parseXml(vcardString).element("vcard");
			vcard.addNamespace("", VCard.NAMESPACE_URI);
			vcard.detach();
			responseItem.add(vcard);
		} catch (DocumentException e) {
			logger.error(e);
			setErrorCondition(PacketError.Type.wait,
					PacketError.Condition.internal_server_error);
		}
	}
}