package com.surevine.profileserver.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.surevine.profileserver.db.exception.NodeStoreException;
import org.xmpp.packet.JID;
import org.xmpp.resultsetmanagement.ResultSet;

/**
 * NodeStore is a interface for classes which have the ability to store and
 * retrieve nodes, and user affiliations and subscriptions to those nodes.
 */
public interface NodeStore {
	
	/**
	 * Return whether a user has a record in the database
	 */
	boolean hasUser(JID user);
	
    /**
     * Adds an owner to the system
     * 
     * @param JID jid
     */
	void addOwner(JID jid) throws NodeStoreException;
	
	/**
	 * Closes this node store instance and releases any resources.
	 */
	void close() throws NodeStoreException;
	
	/**
	 * Begins an atomic transaction. The transaction will include any operations
	 * carried out on this object until either {@link #commitTransaction()} or
	 * {@link #rollbackTransaction()} is called. Can be called multiple times to
	 * invoke a sort of stack of transactions. The transaction will then only be
	 * committed if {@link #commitTransaction()} has been called the same number
	 * of times that {@link #beginTransaction()} was called. If
	 * {@link #closeTransaction()}
	 * 
	 * @return the transaction object which can be used to commit or rollback
	 *         the transaction.
	 * @throws NodeStoreException
	 * @throws IllegalStateException
	 *             if a failed (i.e. rolled back) transaction is in progress
	 */
	Transaction beginTransaction() throws NodeStoreException;

	/**
	 * A {@link NodeStore} transaction.
	 */
	public interface Transaction {

		/**
		 * Commits and closes the transaction.
		 * 
		 * @throws NodeStoreException
		 * 
		 * @throws IllegalStateException
		 *             if the transaction has already been closed.
		 */
		void commit() throws NodeStoreException;

		/**
		 * Closes and rolls back the transaction.
		 * <p>
		 * Silently fails if the transaction has already been committed so that
		 * it can safely be used in a finally block. e.g:
		 * <p>
		 * <blockquote>
		 * 
		 * <pre>
		 * Transaction transaction = null;
		 * 
		 * try {
		 *   transaction = nodeStore.beginTransaction();
		 * 
		 *   ... Do some stuff ...
		 *   
		 *   transaction.commit();
		 * } finally {
		 *   if(transaction != null) transaction.close();
		 * }
		 * </pre>
		 * 
		 * </blockquote>
		 */
		void close() throws NodeStoreException;
	}
}