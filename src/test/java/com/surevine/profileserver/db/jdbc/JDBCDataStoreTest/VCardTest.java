package com.surevine.profileserver.db.jdbc.JDBCDataStoreTest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.packet.JID;

import com.surevine.profileserver.db.jdbc.DatabaseTester;
import com.surevine.profileserver.db.jdbc.JDBCDataStore;
import com.surevine.profileserver.db.jdbc.dialect.Sql92DataStoreDialect;
import com.surevine.profileserver.helpers.IQTestHandler;

public class VCardTest {

	DatabaseTester dbTester;
	Connection conn;

	JDBCDataStore store;
	private JID ownerJid = new JID("owner@example.com");
	private JID userJid = new JID("user@server.co.uk/desktop");
	private JID noPublicJid = new JID("nopublic@server.com");
	private String group = "friends";

	public VCardTest() throws SQLException, IOException, ClassNotFoundException {
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
	public void testCanGetPublicVCard() throws Exception {
		dbTester.loadData("basic-data");
		Assert.assertEquals("<public-true-2/>", store.getPublicVcard(ownerJid));
	}

	@Test
	public void testGetNullWhenNoPublicVcard() throws Exception {
		dbTester.loadData("basic-data");
		store.addOwner(noPublicJid);
		Assert.assertNull(store.getPublicVcard(noPublicJid));
	}

	@Test
	public void testGetExpectedVCardForUserInRosterGroup() throws Exception {
		dbTester.loadData("basic-data");
		Assert.assertEquals("<advisor-false-5/>",
				store.getVcardForUser(ownerJid, new JID("mum@example.com")));
	}

	@Test
	public void testGetNullWhenNoAppropriateVCardForUser() throws Exception {
		dbTester.loadData("basic-data");
		Assert.assertNull(store.getVcardForUser(ownerJid, ownerJid));
	}
}