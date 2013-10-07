package com.surevine.profileserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.xmpp.packet.JID;

public class Configuration extends Properties {
	private static final Logger logger = Logger.getLogger(Configuration.class);

	private static final long serialVersionUID = 1L;

	private static final String ARRAY_PROPERTY_SEPARATOR = ";";

	public static final String CONFIGURATION_SERVER_DOMAIN = "server.domain";
	private static final String CONFIGURATION_FILE = "configuration.properties";
	
	private static Configuration instance = null;

	private Properties conf;

	private Configuration() {
		try {
			conf = new Properties();
			File f = new File(CONFIGURATION_FILE);

			if (f.exists()) {
				logger.info("Found " + CONFIGURATION_FILE
						+ " in working directory.");
				load(new FileInputStream(f));
			} else {
				// Otherwise attempt to load it from the classpath
				logger.info("No "
						+ CONFIGURATION_FILE
						+ " found in working directory. Attempting to load from classpath.");
				load(this.getClass().getClassLoader()
						.getResourceAsStream(CONFIGURATION_FILE));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	public static Configuration getInstance() {
		if (null == instance) {
			instance = new Configuration();
		}
		return instance;
	}

	public String getProperty(String key) {
		return conf.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return conf.getProperty(key, defaultValue);
	}

	public void load(InputStream inputStream) throws IOException {
		conf.load(inputStream);
	}

	private Collection<String> getStringArrayProperty(String key) {
		String prop = getProperty(key);

		if (null == prop) {
			return Collections.emptyList();
		}

		return Arrays.asList(prop.split(ARRAY_PROPERTY_SEPARATOR));
	}

	private Collection<JID> getJIDArrayProperty(String key) {
		Collection<String> props = getStringArrayProperty(key);

		Collection<JID> jids = new ArrayList<JID>(props.size());

		for (String prop : props) {
			jids.add(new JID(prop));
		}

		return jids;
	}

	public String getServerDomain() {
		return getProperty(CONFIGURATION_SERVER_DOMAIN);
	}

	public boolean getBooleanProperty(
			final String key, final boolean defaultValue) {
		String value = getProperty(key);
		
		if(value != null) {
			if(value.equalsIgnoreCase("true")) {
				return true;
			}
			if(value.equalsIgnoreCase("false")) {
				return false;
			}
			logger.warn("Invalid boolean property value for " + key + ": " + value);
		}
		return defaultValue;
	}
}