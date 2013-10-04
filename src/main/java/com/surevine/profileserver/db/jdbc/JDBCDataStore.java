package com.surevine.profileserver.db.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xmpp.packet.JID;
import org.xmpp.resultsetmanagement.ResultSet;
import org.xmpp.resultsetmanagement.ResultSetImpl;

import com.surevine.profileserver.Configuration;
import com.surevine.profileserver.db.DataStore;
import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.db.jdbc.dialect.Sql92DataStoreDialect;

public class JDBCDataStore implements DataStore {

	private Logger logger = Logger.getLogger(JDBCDataStore.class);
	private final Connection conn;
	private final Sql92DataStoreDialect dialect;
	private final Deque<JDBCTransaction> transactionStack;
	private boolean transactionHasBeenRolledBack = false;

	/**
	 * Create a new node store connection backed by the given JDBC
	 * {@link Connection}.
	 * 
	 * @param conn
	 *            the connection to the backing database.
	 */
	public JDBCDataStore(final Connection conn,
			final Sql92DataStoreDialect dialect) {
		this.conn = conn;
		this.dialect = dialect;
		transactionStack = new ArrayDeque<JDBCTransaction>();
	}


	@Override
	public boolean hasOwner(JID jid) throws DataStoreException {
		PreparedStatement existsStatement = null;
		try {
			existsStatement = conn.prepareStatement(dialect.selectOwner());
			existsStatement.setString(1, jid.toBareJID());
			java.sql.ResultSet rs = existsStatement.executeQuery();

			boolean exists = rs.next();
            rs.close();
			existsStatement.close();
            
			return exists;
		} catch (SQLException e) {
			throw new DataStoreException(e);
		} finally {
			close(existsStatement); // Will implicitly close the resultset if
									// required
		}
	}
	
	@Override
	public void addOwner(JID jid) throws DataStoreException {
		PreparedStatement addStatement = null;
		try {
			addStatement = conn.prepareStatement(dialect.addOwner());
			addStatement.setString(1, jid.toBareJID());
			addStatement.executeUpdate();
			addStatement.close();
		} catch (SQLException e) {
			throw new DataStoreException(e);
		} finally {
			close(addStatement);
		}
	}
	
    @Override
	public void removeOwner(JID jid) throws DataStoreException {
		PreparedStatement deleteStatement = null;
		try {
			deleteStatement = conn.prepareStatement(dialect.deleteOwner());
			deleteStatement.setString(1, jid.toBareJID());
			deleteStatement.execute();
			deleteStatement.close();
		} catch (SQLException e) {
			throw new DataStoreException(e);
		} finally {
			close(deleteStatement);
		}
	}
	
	@Override
	public Transaction beginTransaction() throws DataStoreException {
		if (transactionHasBeenRolledBack) {
			throw new IllegalStateException(
					"The transaction has already been rolled back");
		}

		JDBCTransaction transaction;
		try {
			transaction = new JDBCTransaction(this);
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
		return transaction;
	}

	private void close(final Statement stmt) {
		if (stmt != null) {
			try {
				if (false == stmt.isClosed())
					stmt.close();
				// stmt.getConnection().close();
			} catch (SQLException e) {
				logger.error(
						"SQLException thrown while trying to close a statement",
						e);
			}
		}
	}

	private void close(final Transaction trans) throws DataStoreException {
		if (trans != null) {
			trans.close();
		}
	}

	public class JDBCTransaction implements Transaction {
		private JDBCDataStore store;
		private boolean closed;

		private JDBCTransaction(final JDBCDataStore store) throws SQLException {
			this.store = store;
			closed = false;

			if (store.transactionStack.isEmpty()) {
				store.conn.setAutoCommit(false);
			}

			store.transactionStack.push(this);
		}

		@Override
		public void commit() throws DataStoreException {
			if (closed) {
				throw new IllegalStateException(
						"Commit called on transaction that is already closed");
			}
			if (!isLatestTransaction()) {
				throw new IllegalStateException(
						"Commit called on transaction other than the innermost transaction");
			}
			if (store.transactionHasBeenRolledBack) {
				throw new IllegalStateException(
						"Commit called after inner transaction has already been rolled back");
			}

			store.transactionStack.pop();
			closed = true;

			try {
				if (store.transactionStack.isEmpty()) {
					store.conn.commit();
					store.conn.setAutoCommit(true);
					store.transactionHasBeenRolledBack = false;
				}
			} catch (SQLException e) {
				throw new DataStoreException(e);
			}
		}

		@Override
		public void close() throws DataStoreException {
			if (closed) {
				return; // Do nothing nicely and silently
			}

			if (!isLatestTransaction()) {
				throw new IllegalStateException(
						"Close called on transaction other than the innermost transaction");
			}

			store.transactionStack.pop();
			closed = true;
			store.transactionHasBeenRolledBack = true;

			try {
				if (store.transactionStack.isEmpty()) {
					store.conn.rollback();
					store.conn.setAutoCommit(true);
					store.transactionHasBeenRolledBack = false;
				}
			} catch (SQLException e) {
				throw new DataStoreException(e);
			}
		}

		private boolean isLatestTransaction() {
			return (store.transactionStack.peek() == this);
		}
	}

	@Override
	public void close() throws DataStoreException {
		try {
			conn.close();
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	public interface DataStoreSQLDialect {

		String selectOwner();

		String deleteOwner();

		String addOwner();

	}
}