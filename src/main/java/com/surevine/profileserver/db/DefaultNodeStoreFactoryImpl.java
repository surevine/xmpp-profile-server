package com.surevine.profileserver.db;

import java.sql.Connection;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.surevine.profileserver.db.exception.NodeStoreException;
import com.surevine.profileserver.db.jdbc.JDBCNodeStore;
import com.surevine.profileserver.db.jdbc.dialect.Sql92NodeStoreDialect;

public class DefaultNodeStoreFactoryImpl implements NodeStoreFactory {

	private static final String CONFIGURATION_JDBC_DIALECT = "jdbc.dialect";
	private static final Logger LOGGER = Logger.getLogger(DefaultNodeStoreFactoryImpl.class);
	
	private final Properties configuration;

	public DefaultNodeStoreFactoryImpl(final Properties configuration)
			throws NodeStoreException {
		this.configuration = configuration;

		String dialectClass = configuration.getProperty(CONFIGURATION_JDBC_DIALECT,
				Sql92NodeStoreDialect.class.getName());

		try {
			Class.forName(dialectClass).newInstance();
		} catch (Exception e) {
			throw new NodeStoreException("Could not instantiate dialect class "
					+ dialectClass, e);
		}
	}

	@Override
	public NodeStore create() {

		Connection connection = null;
		try {
			connection = new JDBCConnectionFactory(configuration).getConnection();
			return new JDBCNodeStore(
					connection,
					new Sql92NodeStoreDialect());
		} catch (Exception e) {
			LOGGER.error("JDBCNodeStore failed to initialize.", e);
		}
		return null;
	}

}
