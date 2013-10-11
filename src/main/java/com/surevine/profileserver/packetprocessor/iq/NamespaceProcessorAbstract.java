package com.surevine.profileserver.packetprocessor.iq;

import java.io.StringReader;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.PacketError.Condition;
import org.xmpp.packet.PacketError.Type;

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.packetprocessor.PacketProcessor;
import com.surevine.profileserver.packetprocessor.iq.namespace.surevine.Surevine;

public abstract class NamespaceProcessorAbstract
    implements PacketProcessor<IQ>
{
	public static final String NS_RSM = "http://jabber.org/protocol/rsm";
	public static final String NS_XMPP_STANZAS  = "urn:ietf:params:xml:ns:xmpp-stanzas";
    
	public static Logger logger = Logger.getLogger(NamespaceProcessorAbstract.class);
	
    protected BlockingQueue<Packet> outQueue;
    protected DataStore        dataStore;
    protected Element          element;
    protected IQ               response;
    protected IQ               request;

	protected Element resultSetManagement;
	protected String firstItem;
	protected String lastItem;
	protected int totalEntriesCount;

	protected Properties configuration;
	private String serverDomain;
	
	public NamespaceProcessorAbstract(BlockingQueue<Packet> outQueue, Properties configuration, DataStore dataStore) {
		this.setOutQueue(outQueue);
		this.configuration = configuration;
		this.setDataStore(dataStore);
    }

	public void setOutQueue(BlockingQueue<Packet> outQueue)
	{
		this.outQueue = outQueue;
	}

	public void setDataStore(DataStore dataStore)
	{
		this.dataStore = dataStore;
	}
	
	public void setServerDomain(String domain)
	{
		serverDomain = domain;
	}

	protected String getServerDomain()
	{
		if (null == serverDomain) {
            serverDomain = Configuration.getInstance()
			    .getProperty("server.domain");
		}
		return serverDomain;
	}

	protected void setErrorCondition(Type type, Condition condition)
	{
		if (null == response) response = IQ.createResultIQ(request);
		response.setType(IQ.Type.error);
		PacketError error = new PacketError(condition, type);
		response.setError(error);
	}
	
	protected void createExtendedErrorReply(Type type, Condition condition,
			String additionalElement) {
		if (null == response) response = IQ.createResultIQ(request);
		response.setType(IQ.Type.error);
		Element standardError = new DOMElement(condition.toXMPP(),
				new org.dom4j.Namespace("", NS_XMPP_STANZAS));
		Element extraError = new DOMElement(additionalElement,
				new org.dom4j.Namespace("", Surevine.NAMESPACE_URI));
		Element error = new DOMElement("error");
		error.addAttribute("type", type.toXMPP());
		error.add(standardError);
		error.add(extraError);
		response.setChildElement(error);
	}
	
    protected Element parseXml(String stanzaStr) throws DocumentException {
    	SAXReader xmlReader = new SAXReader();
        xmlReader.setMergeAdjacentText(true);
        xmlReader.setStringInternEnabled(true);
        xmlReader.setStripWhitespaceText(true);
        return xmlReader.read(new StringReader(stanzaStr)).getRootElement();
	}
}