package com.surevine.profileserver.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.surevine.profileserver.db.exception.DataStoreException;
import com.surevine.profileserver.model.VCardMeta;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.resultsetmanagement.ResultSet;

/**
 * DataStore is a interface for classes which have the ability to store and
 * retrieve nodes, and user affiliations and subscriptions to those nodes.
 */
public interface DataStore {
	
	/**
	 * Return whether a user has a record in the database
	 * @throws DataStoreException 
	 */
	boolean hasOwner(JID user) throws DataStoreException;
	
    /**
     * Adds an owner to the system
     * 
     * @param JID jid
     */
	void addOwner(JID jid) throws DataStoreException;
	
    /**
     * Remove an owner from the system
     * 
     * @param ownerJid
     * @throws DataStoreException 
     */
	void removeOwner(JID ownerJid) throws DataStoreException;
	
    /**
     * Add a roster entry for an owner
     * 
     * @param owner
     * @param user
     * @param group
     * @throws DataStoreException
     */
	void addRosterEntry(JID owner, JID user, String group)
			throws DataStoreException;
	
    /**
     * Get the roster group for an owner
     *
     * @param owner
     * @param user
     * @return
     * @throws DataStoreException
     */
	ArrayList<String> getRosterGroupsForUser(JID owner, JID user) throws DataStoreException;

    /**
     * Get the roster group for an owner
     *
     * @param owner
     * @param vcard
     * @return
     * @throws DataStoreException
     */
	List<String> getRosterGroupsForVCard(JID owner, String vcard)
			throws DataStoreException;
	
	/**
	 * Get a list of roster groups for an owner
	 * 
	 * @param owner
	 * @return
	 * @throws DataStoreException
	 */
	List<String> getOwnerRosterGroupList(JID owner) throws DataStoreException;
	
    /**
     * Get a public vCard
     * 
     * @param owner
     * @return
     */
	String getPublicVcard(JID owner) throws DataStoreException;
	
	/**
	 * Get a specific vCard
	 * 
	 * @param owner
	 * @param name
	 * @throws DataStoreException 
	 */
	String getVcard(JID owner, String name) throws DataStoreException;
	
    /**
     * Get vCard for a specific user
     * 
     * @param owner
     * @param user
     * @return
     * @throws DataStoreException
     */
	String getVcardForUser(JID owner, JID user) throws DataStoreException;
	
    /**
     * Get vcard meta data
     * 
     * @param from
     * @param name
     * @return
     * @throws DataStoreException 
     */
	VCardMeta getVCardMeta(JID from, String name) throws DataStoreException;
	
    /**
     * Clear an owner's roster from the system
     * 
     * @param owner
     * @throws DataStoreException
     */
	void clearRoster(JID owner) throws DataStoreException;
	
	/**
	 * Save vcard for owner
	 * 
	 * @param owner
	 * @param name  VCard name
	 * @param vcard VCard data
	 * @throws DataStoreException 
	 */
	void saveVcard(JID owner, String name, String vcard) throws DataStoreException;
	
	/**
	 * Closes this node store instance and releases any resources.
	 */
	void close() throws DataStoreException;
	
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
	 * @throws DataStoreException
	 * @throws IllegalStateException
	 *             if a failed (i.e. rolled back) transaction is in progress
	 */
	Transaction beginTransaction() throws DataStoreException;

	/**
	 * A {@link DataStore} transaction.
	 */
	public interface Transaction {

		/**
		 * Commits and closes the transaction.
		 * 
		 * @throws DataStoreException
		 * 
		 * @throws IllegalStateException
		 *             if the transaction has already been closed.
		 */
		void commit() throws DataStoreException;

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
		 *   transaction = dataStore.beginTransaction();
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
		void close() throws DataStoreException;
	}

}