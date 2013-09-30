package com.surevine.profileserver;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main {
    
	private static Logger logger = Logger.getLogger(Main.class);
	
    public static void main(String[] args) {
        try {
        	startComponents();
		} catch (Exception e) {
			logger.error("Failed during initialization.", e);
		}
    }

	private static void startComponents() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
        logger.info("Starting Buddycloud channel mockup version...");

    	Configuration conf = Configuration.getInstance(); 

        logger.info("Connecting to '" + conf.getProperty("xmpp.host") + ":" 
            + conf.getProperty("xmpp.port") 
            + "' and trying to claim address '" 
            + conf.getProperty("server.domain") + "'.");

        String domain = conf.getProperty("server.domain");
        
        if (domain == null) {
        	throw new IllegalArgumentException("Property server.domain is mandatory.");
        }
        new XmppComponent(conf, domain).run();	
        hang();
	}

	private static void hang() {
		while (true) {
		    try {
		        Thread.sleep(5000);
		    } catch (InterruptedException e) {
		    	logger.error(e);
		    }
		}
	}
}