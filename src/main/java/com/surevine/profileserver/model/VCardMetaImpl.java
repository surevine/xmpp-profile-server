package com.surevine.profileserver.model;

import java.util.Date;

public class VCardMetaImpl implements VCardMeta {

	private final String name;
	private final Date lastUpdated;
	private final boolean isDefault;
	
	public VCardMetaImpl(String name, Date lastUpdated, boolean isDefault) {
		this.name = name;
		this.lastUpdated = lastUpdated;
		this.isDefault = isDefault;	
	}
	
	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public String defaultAttribute() {
		return (true == isDefault) ? "true" : "false";
	}

	@Override
	public Date lastUpdated() {
		return lastUpdated;
	}

	@Override
	public String getName() {
		return name;
	}

}
