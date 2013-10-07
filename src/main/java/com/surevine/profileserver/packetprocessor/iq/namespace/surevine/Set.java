package com.surevine.profileserver.packetprocessor.iq.namespace.surevine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
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
import com.surevine.profileserver.packetprocessor.iq.namespace.command.Command;
import com.surevine.profileserver.packetprocessor.iq.namespace.register.Register;

public class Set extends NamespaceProcessorAbstract {

	public static final String ELEMENT_NAME = "query";
	private static final Logger logger = Logger.getLogger(Set.class);

	private Element query;
	private Map<String, String> conf;

	public Set(BlockingQueue<Packet> outQueue, Properties configuration,
			DataStore dataStore) {
		super(outQueue, configuration, dataStore);
	}

	@Override
	public void process(IQ reqIQ) throws Exception {

		request = reqIQ;
		response = IQ.createResultIQ(reqIQ);

		if (null == request.getChildElement().element("update")) {
			setErrorCondition(PacketError.Type.modify,
					PacketError.Condition.bad_request);
		} else {
			handleUpdateRequest();
		}
		outQueue.put(response);
	}

	private void handleUpdateRequest() throws InterruptedException {

		try {
			if (false == dataStore.hasOwner(request.getFrom())) {
				setErrorCondition(PacketError.Type.auth,
						PacketError.Condition.registration_required);
				return;
			}
			sendRosterRetrieval();
		} catch (DataStoreException e) {
			logger.error(e);
			setErrorCondition(PacketError.Type.wait,
					PacketError.Condition.internal_server_error);
		}
	}

	private void sendRosterRetrieval() throws InterruptedException {
		IQ rosterRequest = getRosterRequestIq();
        Element command = rosterRequest.getElement().addElement("command", Command.NAMESPACE_URI);
        command.addAttribute("node", Command.GET_USER_ROSTER);
        command.addAttribute("sessionid",  getSessionId());
        
        DataForm x = new DataForm(DataForm.Type.submit);
        
        FormField formType = x.addField("FORM_TYPE", null, FormField.Type.hidden);
        formType.addValue(Command.FORM_TYPE);
        
        FormField jid = x.addField("accountjids", null, null);
        jid.addValue(request.getFrom().toBareJID().toString()); 

        command.add(x.getElement());
		outQueue.put(rosterRequest);

	}

	private IQ getRosterRequestIq() {
		IQ iq = new IQ();
		iq.setFrom(configuration
				.getProperty(Configuration.CONFIGURATION_SERVER_DOMAIN));
		iq.setTo(request.getFrom().getDomain());
		iq.setID(request.getID() + "-roster");
		iq.setType(IQ.Type.set);
		return iq;
	}

	private String getSessionId() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
		String date = df.format(new Date());
		return "roster:" + date;
	}
}