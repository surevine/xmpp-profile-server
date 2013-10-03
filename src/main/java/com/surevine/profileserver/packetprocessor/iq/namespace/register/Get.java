package com.surevine.profileserver.packetprocessor.iq.namespace.register;

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
import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.NamespaceProcessorAbstract;
import com.surevine.profileserver.packetprocessor.iq.namespace.register.Register;

public class Get extends NamespaceProcessorAbstract {

	public static final String ELEMENT_NAME = "query";
	private static final Logger logger = Logger.getLogger(Get.class);

	private Element query;
	private Map<String, String> conf;

	public Get(BlockingQueue<Packet> outQueue, Properties configuration,
			NodeStore nodeStore) {
		super(outQueue, configuration, nodeStore);
	}

	@Override
	public void process(IQ reqIQ) throws Exception {

		request = reqIQ;
		response = IQ.createResultIQ(reqIQ);

		boolean isLocalUser = request
				.getFrom()
				.getDomain()
				.contains(
						configuration
								.getProperty(Configuration.CONFIGURATION_SERVER_DOMAIN));
		if (false == isLocalUser) {
			createExtendedErrorReply(PacketError.Type.cancel,
					PacketError.Condition.not_allowed, "not-local-jid");
			outQueue.put(response);
		}

	}
}