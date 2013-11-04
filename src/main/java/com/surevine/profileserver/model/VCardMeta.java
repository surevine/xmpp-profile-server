package com.surevine.profileserver.model;

import java.util.Date;

public interface VCardMeta {
	
	/**
	 * Is default VCard?
	 */
	public boolean isDefault();
	
	/**
	 * Get default attribute value
	 */
	public String defaultAttribute();
	
	/**
	 * Get last updated date
	 */
	public Date lastUpdated();
	
	/**
	 * Get vcard name
	 */
	public String getName();
}
