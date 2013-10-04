package com.surevine.profileserver.db;

import java.sql.Connection;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.db.jdbc.JDBCNodeStore;
import com.surevine.profileserver.db.jdbc.dialect.Sql92DataStoreDialect;

public class DataStoreFactoryImpl implements DataStoreFactory {

	private static final String CONFIGURATION_JDBC_DIALECT = "jdbc.dialect";
	private static final Logger LOGGER = Logger.getLogger(DataStoreFactoryImpl.class);
	
	private final Properties configuration;

	public DataStoreFactoryImpl(final Properties configuration)
			throws DataStoreException {
		this.configuration = configuration;

		String dialectClass = configuration.getProperty(CONFIGURATION_JDBC_DIALECT,
				Sql92DataStoreDialect.class.getName());

		try {
			Class.forName(dialectClass).newInstance();
		} catch (Exception e) {
			throw new DataStoreException("Could not instantiate dialect class "
					+ dialectClass, e);
		}
	}

	@Override
	public DataStore create() {

		Connection connection = null;
		try {
			connection = new JDBCConnectionFactory(configuration).getConnection();
			return new JDBCNodeStore(
					connection,
					new Sql92DataStoreDialect());
		} catch (Exception e) {
			LOGGER.error("JDBCNodeStore failed to initialize.", e);
		}
		return null;
	}

}
