package com.surevine.profileserver.packetprocessor.iq.namespace.command;

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

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.namespace.register.Register;
import com.surevine.profileserver.packetprocessor.iq.namespace.surevine.Surevine;

public class Result implements PacketProcessor<IQ> {

	public static final String ELEMENT_NAME = "query";
	private static final Logger logger = Logger.getLogger(Result.class);
	private final BlockingQueue<Packet> outQueue;
	private final DataStore dataStore;
	private String node;
	private IQ result;
	private IQ requestIq;
	private Element query;
	private Map<String, String> conf;

	public Result(BlockingQueue<Packet> outQueue, Properties configuration, DataStore dataStore) {
		this.outQueue = outQueue;
		this.dataStore = dataStore;
	}

	@Override
	public void process(IQ reqIQ) throws Exception {
		requestIq = reqIQ;
		Element elm = reqIQ.getChildElement();

	}

}