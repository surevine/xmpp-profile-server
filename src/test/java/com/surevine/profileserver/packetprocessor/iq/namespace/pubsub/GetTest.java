package com.surevine.profileserver.packetprocessor.iq.namespace.pubsub;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.helpers.IQTestHandler;
import com.surevine.profileserver.model.VCardMeta;
import com.surevine.profileserver.model.VCardMetaImpl;
import com.surevine.profileserver.packetprocessor.iq.namespace.vcard.VCard;

public class GetTest extends IQTestHandler {

	private DataStore dataStore;
	private Get vcard;
	private LinkedBlockingQueue<Packet> queue;

	private IQ itemsRequest;
	private IQ itemRequest;
	private IQ configureRequest;

	private ArrayList<String> groups;

	@Before
	public void setUp() throws Exception {
		dataStore = Mockito.mock(DataStore.class);
		queue = new LinkedBlockingQueue<Packet>();

		vcard = new Get(queue, readConf(), dataStore);

		itemsRequest = readStanzaAsIq("/pubsub/items");
		itemRequest = readStanzaAsIq("/pubsub/item");
		configureRequest = readStanzaAsIq("/pubsub/configuration-get");

		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(
				true);
	}

	@Test
	public void testDataStoreExceptionReturnsExpectedError() throws Exception {

		Mockito.doThrow(new DataStoreException()).when(dataStore)
				.hasOwner(Mockito.any(JID.class));

		vcard.process(itemsRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.wait, error.getType());

		Assert.assertEquals(PacketError.Condition.internal_server_error,
				error.getCondition());
	}

	@Test
	public void testUnknownOwnerRequestedToRegister() throws Exception {

		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(
				false);

		vcard.process(itemsRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.auth, error.getType());

		Assert.assertEquals(PacketError.Condition.registration_required,
				error.getCondition());

	}

	@Test
	public void testUnknownChildElementResultsInErrorResponse()
			throws Exception {

		IQ modifiedRequest = itemsRequest.createCopy();
		modifiedRequest.getChildElement().element("items").detach();

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.cancel, error.getType());

		Assert.assertEquals(PacketError.Condition.feature_not_implemented,
				error.getCondition());
	}

	@Test
	public void testReceiveErrorIfNodeIsNotEqualToVCardNamespace()
			throws Exception {
		IQ modifiedRequest = itemsRequest.createCopy();
		modifiedRequest.getChildElement().element("items").attribute("node")
				.setValue("not-vcard-namespace");

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
	}

	@Test
	public void testRequestingSingleVCardWithNoIdReturnsError()
			throws Exception {
		IQ modifiedRequest = itemRequest.createCopy();
		modifiedRequest.getChildElement().element("items").element("item")
				.attribute("id").detach();

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());

		Assert.assertEquals(vcard.MISSING_VCARD_ID,
				error.getApplicationConditionName());
	}

	@Test
	public void testRequestingSingleVCardWithEmptyIdReturnsError()
			throws Exception {
		IQ modifiedRequest = itemRequest.createCopy();
		modifiedRequest.getChildElement().element("items").element("item")
				.attribute("id").setValue("");

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
		Assert.assertEquals(vcard.MISSING_VCARD_ID,
				error.getApplicationConditionName());
	}

	@Test
	public void testRequestingVCardWhichDoesntExistReturnsItemNotFound()
			throws Exception {

		Mockito.when(
				dataStore.getVcard(Mockito.any(JID.class), Mockito.anyString()))
				.thenReturn(null);

		vcard.process(itemRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.cancel, error.getType());

		Assert.assertEquals(PacketError.Condition.item_not_found,
				error.getCondition());
	}

	@Test
	public void testRequestingSingleVCardReturnsExpectedData() throws Exception {

		VCardMeta meta = new VCardMetaImpl("group-name", new Date(), true);

		Mockito.when(
				dataStore.getVcard(Mockito.any(JID.class), Mockito.anyString()))
				.thenReturn(readStanzaAsString("/vcard/public-vcard"));
		Mockito.when(
				dataStore.getVCardMeta(Mockito.any(JID.class),
						Mockito.anyString())).thenReturn(meta);
		vcard.process(itemRequest);

		IQ response = (IQ) queue.poll();
		Element pubsub = response.getElement().element("pubsub");
		Element items = pubsub.element("items");
		Element item = items.element("item");

		Assert.assertEquals(PubSub.NAMESPACE_URI, pubsub.getNamespaceURI());
		Assert.assertEquals(VCard.NAMESPACE_URI, items.attributeValue("node"));
		Assert.assertEquals("family", item.attributeValue("id"));

		Assert.assertEquals("true", item.attributeValue("profile:default"));

		Assert.assertNotNull(item.element("vcard"));
	}

	@Test
	public void testCanRetrieveVCardsWhereThereAreNone() throws Exception {

		Mockito.when(dataStore.getVCardList(Mockito.any(JID.class)))
				.thenReturn(new ArrayList<VCardMeta>());
		vcard.process(itemsRequest);

		IQ response = (IQ) queue.poll();

		Assert.assertEquals(IQ.Type.result, response.getType());

		Element pubsub = response.getElement().element("pubsub");
		Assert.assertEquals(PubSub.NAMESPACE_URI, pubsub.getNamespaceURI());

		Element items = pubsub.element("items");
		Assert.assertEquals(VCard.NAMESPACE_URI, items.attributeValue("node"));

		Assert.assertEquals(0, items.elements("item").size());
	}

	@Test
	public void testCanRetrieveVCardList() throws Exception {

		ArrayList<VCardMeta> metas = new ArrayList<VCardMeta>();
		metas.add(new VCardMetaImpl("advisor", new Date(), false));
		metas.add(new VCardMetaImpl("family", new Date(), false));
		metas.add(new VCardMetaImpl("friends", new Date(), false));
		metas.add(new VCardMetaImpl("colleagues", new Date(), false));
		metas.add(new VCardMetaImpl("public", new Date(), true));

		Mockito.when(dataStore.getVCardList(Mockito.any(JID.class)))
				.thenReturn(metas);

		vcard.process(itemsRequest);

		IQ response = (IQ) queue.poll();

		Assert.assertEquals(IQ.Type.result, response.getType());

		Element pubsub = response.getElement().element("pubsub");
		Assert.assertEquals(PubSub.NAMESPACE_URI, pubsub.getNamespaceURI());

		Element items = pubsub.element("items");
		Assert.assertEquals(VCard.NAMESPACE_URI, items.attributeValue("node"));

		Assert.assertEquals(5, items.elements("item").size());

		Assert.assertEquals("advisor",
				((Element) items.elements("item").get(0)).attributeValue("id"));
		Assert.assertEquals("family",
				((Element) items.elements("item").get(1)).attributeValue("id"));
		Assert.assertEquals("friends",
				((Element) items.elements("item").get(2)).attributeValue("id"));

	}

	@Test
	public void testNoNodeAttributeOnConfigureReturnsError() throws Exception {
		IQ modifiedRequest = configureRequest.createCopy();
		modifiedRequest.getChildElement().element("configure")
				.attribute("node").detach();

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
	}

	@Test
	public void testEmptyNodeAttributeOnConfigureReturnsError()
			throws Exception {
		IQ modifiedRequest = configureRequest.createCopy();
		modifiedRequest.getChildElement().element("configure")
				.attribute("node").setValue("not-vcard-namespace");

		vcard.process(modifiedRequest);

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());
	}

	@Test
	public void testReturnsExpectedDataFormWithNoVCards() throws Exception {

		Mockito.when(dataStore.getVCardList(Mockito.any(JID.class)))
				.thenReturn(new ArrayList<VCardMeta>());

		ArrayList<String> groups = new ArrayList<String>();
		groups.add("family");
		groups.add("friends");
		groups.add("colleagues");
		Mockito.when(dataStore.getOwnerRosterGroupList(Mockito.any(JID.class)))
				.thenReturn(groups);

		vcard.process(configureRequest);

		IQ response = (IQ) queue.poll();

		Assert.assertEquals(IQ.Type.result, response.getType());

		Element pubsub = response.getChildElement();
		Assert.assertEquals(PubSub.NAMESPACE_OWNER, pubsub.getNamespaceURI());
		Element configure = pubsub.element("configure");
		Assert.assertEquals(VCard.NAMESPACE_URI,
				configure.attributeValue("node"));
		Element x = configure.element("x");
		Assert.assertEquals(DataForm.NAMESPACE, x.getNamespaceURI());

		Assert.assertEquals(1, x.elements().size());

		Element field = x.element("field");
		Assert.assertEquals(Get.VCARD_DEFAULT, field.attributeValue("var"));
		Assert.assertEquals(FormField.Type.list_single.toXMPP(),
				field.attributeValue("type"));
		Assert.assertEquals(Get.VCARD_DEFAULT_LABEL,
				field.attributeValue("label"));

		Assert.assertEquals(1, field.elements("option").size());

		Assert.assertEquals(VCard.NONE, ((Element) field.elements("option")
				.get(0)).elementText("value"));
	}

	@Test
	public void testReturnsExpectedDataFormWithVCards() throws Exception {

		ArrayList<VCardMeta> cards = new ArrayList<VCardMeta>();
		cards.add(new VCardMetaImpl("card-family", new Date(), false));
		cards.add(new VCardMetaImpl("card-work", new Date(), false));
		cards.add(new VCardMetaImpl("card-general", new Date(), true));
		Mockito.when(dataStore.getVCardList(Mockito.any(JID.class)))
				.thenReturn(cards);

		ArrayList<String> groups = new ArrayList<String>();
		groups.add("family");
		groups.add("friends");
		groups.add("hugs");
		groups.add("work");
		groups.add("professional");
		Mockito.when(dataStore.getOwnerRosterGroupList(Mockito.any(JID.class)))
				.thenReturn(groups);

		ArrayList<String> cardFamilyRosterGroups = new ArrayList<String>();
		cardFamilyRosterGroups.add("family");
		cardFamilyRosterGroups.add("friends");
		cardFamilyRosterGroups.add("hugs");
		Mockito.when(
				dataStore.getRosterGroupsForVCard(Mockito.any(JID.class),
						Mockito.eq("card-family"))).thenReturn(
				cardFamilyRosterGroups);

		ArrayList<String> cardWorkRosterGroups = new ArrayList<String>();
		cardWorkRosterGroups.add("work");
		cardWorkRosterGroups.add("professional");
		Mockito.when(
				dataStore.getRosterGroupsForVCard(Mockito.any(JID.class),
						Mockito.eq("card-work"))).thenReturn(
				cardWorkRosterGroups);

		ArrayList<String> cardGeneralRosterGroups = new ArrayList<String>();
		Mockito.when(
				dataStore.getRosterGroupsForVCard(Mockito.any(JID.class),
						Mockito.eq("card-general"))).thenReturn(
				cardGeneralRosterGroups);

		vcard.process(configureRequest);

		IQ response = (IQ) queue.poll();

		Assert.assertEquals(IQ.Type.result, response.getType());

		Element pubsub = response.getChildElement();
		Assert.assertEquals(PubSub.NAMESPACE_OWNER, pubsub.getNamespaceURI());
		Element configure = pubsub.element("configure");
		Assert.assertEquals(VCard.NAMESPACE_URI,
				configure.attributeValue("node"));
		Element x = configure.element("x");
		Assert.assertEquals(DataForm.NAMESPACE, x.getNamespaceURI());

		Assert.assertEquals(cards.size() + 1, x.elements().size());

		// Check vcard map fields are correct
		int counter = 0;
		for (VCardMeta card : cards) {
			checkDataFormEntry(card, groups,
					(Element) x.elements("field").get(counter));
			++counter;
		}
		// Manually check their values
		Assert.assertEquals(3, ((Element) x.elements("field")
				.get(0)).elements("value").size());
		Assert.assertEquals("family", ((Element) ((Element) x.elements("field")
				.get(0)).elements("value").get(0)).getText());
		Assert.assertEquals("friends", ((Element) ((Element) x.elements("field")
				.get(0)).elements("value").get(1)).getText());
		Assert.assertEquals("hugs", ((Element) ((Element) x.elements("field")
				.get(0)).elements("value").get(2)).getText());
		
		Assert.assertEquals(2, ((Element) x.elements("field")
				.get(1)).elements("value").size());
		Assert.assertEquals("work", ((Element) ((Element) x.elements("field")
				.get(1)).elements("value").get(0)).getText());
		Assert.assertEquals("professional", ((Element) ((Element) x.elements("field")
				.get(1)).elements("value").get(1)).getText());
		
		Assert.assertEquals(0, ((Element) x.elements("field")
				.get(2)).elements("value").size());
		
		// Check 'default' vcard default element
		Element field = (Element) x.elements("field").get(cards.size());

		Assert.assertEquals(cards.size() + 1, field.elements("option").size());

		Assert.assertEquals(Get.VCARD_DEFAULT, field.attributeValue("var"));
		Assert.assertEquals(FormField.Type.list_single.toXMPP(),
				field.attributeValue("type"));
		Assert.assertEquals(Get.VCARD_DEFAULT_LABEL,
				field.attributeValue("label"));

		Assert.assertEquals(VCard.NONE, ((Element) field.elements("option")
				.get(0)).elementText("value"));

		Assert.assertEquals(cards.get(0).getName(),
				((Element) field.elements("option").get(1))
						.elementText("value"));
		Assert.assertEquals(cards.get(0).getName(),
				((Element) field.elements("option").get(1))
						.attributeValue("label"));

		Assert.assertEquals(cards.get(1).getName(),
				((Element) field.elements("option").get(2))
						.elementText("value"));
		Assert.assertEquals(cards.get(1).getName(),
				((Element) field.elements("option").get(2))
						.attributeValue("label"));

		Assert.assertEquals(cards.get(2).getName(),
				((Element) field.elements("option").get(3))
						.elementText("value"));
		Assert.assertEquals(cards.get(2).getName(),
				((Element) field.elements("option").get(3))
						.attributeValue("label"));
	}

	private void checkDataFormEntry(VCardMeta card, ArrayList<String> groups,
			Element field) {
		Assert.assertEquals(Get.DATAFORM_VARIABLE_PREFIX + card.getName(),
				field.attributeValue("var"));
		Assert.assertEquals(FormField.Type.list_multi.toXMPP(),
				field.attributeValue("type"));

		Assert.assertEquals(String.format(Get.DATAFORM_LABEL, card.getName()),
				field.attributeValue("label"));

		List<Element> options = field.elements("option");
		int counter = 0;
		for (Element option : options) {
			Assert.assertEquals(groups.get(counter),
					option.attributeValue("label"));
			Assert.assertEquals(groups.get(counter),
					option.elementText("value"));
			++counter;
		}

	}
}