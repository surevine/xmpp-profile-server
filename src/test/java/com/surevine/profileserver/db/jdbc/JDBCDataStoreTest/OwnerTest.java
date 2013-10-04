package com.surevine.profileserver.db.jdbc.JDBCDataStoreTest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.packet.JID;

import com.surevine.profileserver.db.jdbc.DatabaseTester;
import com.surevine.profileserver.db.jdbc.JDBCDataStore;
import com.surevine.profileserver.db.jdbc.dialect.Sql92DataStoreDialect;
import com.surevine.profileserver.helpers.IQTestHandler;

@SuppressWarnings("serial")
public class OwnerTest {

	DatabaseTester dbTester;
	Connection conn;

	JDBCDataStore store;
	private JID ownerJid = new JID("owner@example.com");

	public OwnerTest() throws SQLException, IOException,
			ClassNotFoundException {
		dbTester = new DatabaseTester();
		IQTestHandler.readConf();
	}

	@Before
	public void setUp() throws Exception {
		dbTester.initialise();

		store = new JDBCDataStore(dbTester.getConnection(),
				new Sql92DataStoreDialect());
	}

	@After
	public void tearDown() throws Exception {
		dbTester.close();
	}

	@Test
	public void testUnknownOwnerReturnsFalse() throws Exception {
		Assert.assertFalse(store.hasOwner(ownerJid));
	}
	
	@Test
	public void testKnownOwnerReturnsTrue() throws Exception {
		dbTester.loadData("basic-data");
		Assert.assertTrue(store.hasOwner(ownerJid));
	}
	
	@Test
	public void testCanAddOwner() throws Exception {
		Assert.assertFalse(store.hasOwner(ownerJid));
		store.addOwner(new JID("owner@example.com"));
		Assert.assertTrue(store.hasOwner(ownerJid));
	}
	
	@Test
	public void testCanDeleteOwner() throws Exception {
		dbTester.loadData("basic-data");
		Assert.assertTrue(store.hasOwner(ownerJid));
		store.removeOwner(ownerJid);
		Assert.assertFalse(store.hasOwner(ownerJid));
	}
}