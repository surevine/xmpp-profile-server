package com.surevine.profileserver.db.jdbc.JDBCNodeStoreTest;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.xmpp.packet.JID;

import com.surevine.profileserver.db.NodeStore;
import com.surevine.profileserver.db.jdbc.DatabaseTester;
import com.surevine.profileserver.db.jdbc.JDBCNodeStore;
import com.surevine.profileserver.db.jdbc.JDBCNodeStore.NodeStoreSQLDialect;
import com.surevine.profileserver.db.jdbc.dialect.Sql92NodeStoreDialect;
import com.surevine.profileserver.helpers.IQTestHandler;

@SuppressWarnings("serial")
public class OwnerTest {

	DatabaseTester dbTester;
	Connection conn;

	JDBCNodeStore store;

	public OwnerTest() throws SQLException, IOException,
			ClassNotFoundException {
		dbTester = new DatabaseTester();
		IQTestHandler.readConf();
	}

	@Before
	public void setUp() throws Exception {
		dbTester.initialise();

		store = new JDBCNodeStore(dbTester.getConnection(),
				new Sql92NodeStoreDialect());
	}

	@After
	public void tearDown() throws Exception {
		dbTester.close();
	}

	@Test
	public void testUnknownOwnerReturnsFalse() throws Exception {
		Assert.assertFalse(store.hasOwner(new JID("user@example.com")));
	}
	
	@Test
	public void testKnownOwnerReturnsTrue() throws Exception {
		dbTester.loadData("basic-data");
		Assert.assertTrue(store.hasOwner(new JID("owner@example.com")));
	}
}