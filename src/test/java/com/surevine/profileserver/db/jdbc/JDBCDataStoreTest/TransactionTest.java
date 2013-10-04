package com.surevine.profileserver.db.jdbc.JDBCDataStoreTest;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.jdbc.DatabaseTester;
import com.surevine.profileserver.db.jdbc.JDBCDataStore;
import com.surevine.profileserver.db.jdbc.dialect.Sql92DataStoreDialect;
import com.surevine.profileserver.helpers.IQTestHandler;

@SuppressWarnings("serial")
public class TransactionTest {

	DatabaseTester dbTester;
	Connection conn;

	JDBCDataStore store;

	public TransactionTest() throws SQLException, IOException,
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
	public void testBeginTransaction() throws Exception {
		Connection conn = Mockito.mock(Connection.class);
		JDBCDataStore store = new JDBCDataStore(conn,
				mock(Sql92DataStoreDialect.class));

		DataStore.Transaction t = store.beginTransaction();

		assertNotNull("Null transaction returned", t);

		verify(conn).setAutoCommit(false);
	}

	@Test
	public void testCommitTransaction() throws Exception {
		Connection conn = Mockito.mock(Connection.class);
		JDBCDataStore store = new JDBCDataStore(conn,
				mock(Sql92DataStoreDialect.class));

		DataStore.Transaction t = store.beginTransaction();
		t.commit();

		verify(conn).commit();
		verify(conn).setAutoCommit(true);
	}

	@Test
	public void testCloseTransaction() throws Exception {
		Connection conn = Mockito.mock(Connection.class);
		JDBCDataStore store = new JDBCDataStore(conn,
				mock(Sql92DataStoreDialect.class));

		DataStore.Transaction t = store.beginTransaction();
		t.close();

		verify(conn, never()).commit();
		verify(conn).rollback();
		verify(conn).setAutoCommit(true);
	}

	@Test
	public void testCloseOnAlreadyCommittedTransactionDoesntRollback()
			throws Exception {
		Connection conn = Mockito.mock(Connection.class);
		JDBCDataStore store = new JDBCDataStore(conn,
				mock(Sql92DataStoreDialect.class));

		DataStore.Transaction t = store.beginTransaction();
		t.commit();

		t.close();

		verify(conn, never()).rollback();
	}

	@Test
	public void testNestedTransactionsOnlySetAutoCommitOnce() throws Exception {
		Connection conn = Mockito.mock(Connection.class);
		JDBCDataStore store = new JDBCDataStore(conn,
				mock(Sql92DataStoreDialect.class));

		store.beginTransaction();

		// Make sure setAutoCommit was called
		verify(conn).setAutoCommit(false);

		store.beginTransaction();
		store.beginTransaction();

		// Make sure setAutoCommit was still only called once
		verify(conn).setAutoCommit(false);
	}

	@Test
	public void testNestedTransactionsOnlyCallCommitOnOuterTransaction()
			throws Exception {
		Connection conn = Mockito.mock(Connection.class);
		JDBCDataStore store = new JDBCDataStore(conn,
				mock(Sql92DataStoreDialect.class));

		InOrder inOrder = inOrder(conn);

		DataStore.Transaction t1 = store.beginTransaction();
		DataStore.Transaction t2 = store.beginTransaction();
		DataStore.Transaction t3 = store.beginTransaction();

		t3.commit();
		verify(conn, never()).commit(); // Make sure that commit isn't called
										// until the outer transaction is
										// committed

		t2.commit();
		verify(conn, never()).commit(); // Make sure that commit isn't called
										// until the outer transaction is
										// committed

		t1.commit();

		inOrder.verify(conn).commit(); // Make sure that commit was called
		inOrder.verify(conn).setAutoCommit(true);
	}

	@Test(expected = IllegalStateException.class)
	public void testNestedTransactionsWithRollbackInMiddle() throws Exception {
		Connection conn = Mockito.mock(Connection.class);
		JDBCDataStore store = new JDBCDataStore(conn,
				mock(Sql92DataStoreDialect.class));

		DataStore.Transaction t1 = store.beginTransaction();
		DataStore.Transaction t2 = store.beginTransaction();
		DataStore.Transaction t3 = store.beginTransaction();

		t3.commit();
		t2.close();
		t1.commit();
	}

	@Test(expected = IllegalStateException.class)
	public void testNestedTransactionsWithOutOfOrderCommitsThrowsException()
			throws Exception {
		Connection conn = Mockito.mock(Connection.class);
		JDBCDataStore store = new JDBCDataStore(conn,
				mock(Sql92DataStoreDialect.class));

		DataStore.Transaction t1 = store.beginTransaction();
		DataStore.Transaction t2 = store.beginTransaction();
		DataStore.Transaction t3 = store.beginTransaction();

		t3.commit();
		t1.commit(); // t1 must not be committed before t2
		t2.commit();
	}
}