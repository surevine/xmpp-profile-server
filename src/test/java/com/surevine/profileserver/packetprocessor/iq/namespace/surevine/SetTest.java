package com.surevine.profileserver.packetprocessor.iq.namespace.surevine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.apache.bcel.generic.GETSTATIC;
import org.apache.commons.lang.time.DateFormatUtils;
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

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.helpers.IQTestHandler;
import com.surevine.profileserver.packetprocessor.iq.namespace.command.Command;
import com.surevine.profileserver.packetprocessor.iq.namespace.register.Register;

public class SetTest extends IQTestHandler {

	private DataStore dataStore;
	private Set update;
	private LinkedBlockingQueue<Packet> queue;
	private IQ request;
	private Configuration configuration;

	@Before
	public void setUp() throws Exception {
		dataStore = Mockito.mock(DataStore.class);
		queue = new LinkedBlockingQueue<Packet>();
		update = new Set(queue, readConf(), dataStore);
		request = readStanzaAsIq("/surevine/update");
		configuration = Configuration.getInstance();
	}

	@Test
	public void testNoUpdateChildElementReturnsBadRequestError()
			throws Exception {
		request.getChildElement().element("update").detach();
		update.process(request);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.modify, error.getType());

		Assert.assertEquals(PacketError.Condition.bad_request,
				error.getCondition());

		Assert.assertEquals(IQ.Type.error, response.getType());
		Assert.assertEquals(request.getFrom(), response.getTo());
		Assert.assertEquals(request.getID(), response.getID());
	}

	@Test
	public void testDataStoreExceptionReturnsInternalServerError()
			throws Exception {

		Mockito.doThrow(new DataStoreException()).when(dataStore)
				.hasOwner(Mockito.any(JID.class));

		update.process(request);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.wait, error.getType());

		Assert.assertEquals(PacketError.Condition.internal_server_error,
				error.getCondition());

		Assert.assertEquals(IQ.Type.error, response.getType());
		Assert.assertEquals(request.getFrom(), response.getTo());
		Assert.assertEquals(request.getID(), response.getID());
	}

	@Test
	public void testUnregisteredUserReceivesErrorResponse() throws Exception {

		update.process(request);

		Assert.assertEquals(1, queue.size());

		IQ response = (IQ) queue.poll();

		PacketError error = response.getError();
		Assert.assertNotNull(error);
		Assert.assertEquals(PacketError.Type.auth, error.getType());

		Assert.assertEquals(PacketError.Condition.registration_required,
				error.getCondition());

		Assert.assertEquals(IQ.Type.error, response.getType());
		Assert.assertEquals(request.getFrom(), response.getTo());
		Assert.assertEquals(request.getID(), response.getID());
	}

	@Test
	public void testValidRequestGeneratesRosterRetrievalRequest()
			throws Exception {

		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(
				true);

		update.process(request);

		Assert.assertEquals(2, queue.size());

		IQ response = (IQ) queue.poll();
		Assert.assertEquals(IQ.Type.set, response.getType());
		
		Assert.assertEquals(
				configuration.getProperty(Configuration.CONFIGURATION_SERVER_DOMAIN), 
				response.getFrom().toString());

		Assert.assertEquals(request.getID() + "-roster", response.getID());
		Assert.assertEquals(new JID(request.getFrom().getDomain()).toBareJID(),
				response.getTo().toBareJID());
		Element command = response.getElement().element("command");
		Assert.assertNotNull(command);
		Assert.assertEquals(Command.NAMESPACE_URI, command.getNamespaceURI());
		Assert.assertEquals(Command.GET_USER_ROSTER,
				command.attributeValue("node"));

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
		String date = df.format(new Date());

		Assert.assertEquals("roster:" + date,
				command.attributeValue("sessionid"));

		Element x = command.element("x");
		Assert.assertNotNull(x);
		Assert.assertEquals(DataForm.NAMESPACE, x.getNamespaceURI());
		Assert.assertEquals(DataForm.Type.submit.toString(),
				x.attributeValue("type"));
		Assert.assertEquals(2, x.elements("field").size());

		Element formType = (Element) x.elements("field").get(0);
		Assert.assertEquals(FormField.Type.hidden.toString(),
				formType.attributeValue("type"));
		Assert.assertEquals("FORM_TYPE", formType.attributeValue("var"));
		Assert.assertEquals(Command.FORM_TYPE, formType.elementText("value"));

		Element jid = (Element) x.elements("field").get(1);
		Assert.assertEquals("accountjids", jid.attributeValue("var"));
		Assert.assertEquals(request.getFrom().toBareJID(),
				jid.elementText("value"));

		IQ result = (IQ) queue.poll();
		Assert.assertEquals(IQ.Type.result, result.getType());
		Assert.assertEquals(request.getFrom(), result.getTo());
		Assert.assertEquals(request.getID(), result.getID());
	}
}