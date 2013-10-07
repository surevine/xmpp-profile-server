package com.surevine.profileserver.packetprocessor.iq.namespace.command;

import java.util.List;
import java.util.Map;
import java.util.Properties;
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

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.NamespaceProcessorAbstract;
import com.surevine.profileserver.packetprocessor.iq.namespace.register.Register;
import com.surevine.profileserver.packetprocessor.iq.namespace.surevine.Surevine;

public class Result extends NamespaceProcessorAbstract {

	private static final Logger logger = Logger.getLogger(Result.class);
	private String node;
	private IQ result;
	private IQ requestIq;
	private Element query;
	private Map<String, String> conf;
	private Element command;
	private Element x;

	public Result(BlockingQueue<Packet> outQueue, Properties configuration,
			DataStore dataStore) {
		super(outQueue, configuration, dataStore);
	}

	@Override
	public void process(IQ reqIQ) throws Exception {
		requestIq = reqIQ;

		boolean isResultFromLocalServer = configuration.getProperty(
				Configuration.CONFIGURATION_SERVER_DOMAIN).contains(
				requestIq.getFrom().getDomain());

		if (!isResultFromLocalServer) {
			return;
		}

		command = reqIQ.getChildElement();

		if (false == command.attributeValue("node").equals(
				Command.GET_USER_ROSTER)) {
			return;
		}
		if (false == command.attributeValue("status").equals("completed")) {
			return;
		}

		x = command.element("x");
		if (null == x || false == x.getNamespace().getText().equals(Command.FORM_NAMESPACE)) {
			return;
		}

		processForm();
	}

	private void processForm() throws DataStoreException {
		if (!validateForm()) {
			return;
		}
	}

	private boolean validateForm() throws DataStoreException {
		List<Element> fields = x.elements("field");

		String var;
		for (Element field : fields) {
			var = field.attributeValue("var");

			if (var.equals("FORM_TYPE")) {
				if (false == field.elementText("value").equals(
						Command.FORM_TYPE)) {
					return false;
				}
			} else if (var.equals("accountjids")) {
				JID user = new JID(field.elementText("value"));
				if (false == dataStore.hasOwner(user)) {
					return false;
				}
			}
		}

		return true;
	}

}