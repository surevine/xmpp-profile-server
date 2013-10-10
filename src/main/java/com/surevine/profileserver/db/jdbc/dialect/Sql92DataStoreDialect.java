package com.surevine.profileserver.db.jdbc.dialect;

import com.surevine.profileserver.db.jdbc.JDBCDataStore.DataStoreSQLDialect;

public class Sql92DataStoreDialect implements DataStoreSQLDialect {

	private static final String GET_OWNER = "SELECT * FROM owners WHERE \"owner\" = ?;";
	
	private static final String DELETE_OWNER = "DELETE FROM owners where \"owner\" = ?;";

	private static final String ADD_OWNER = "INSERT INTO owners VALUES(?, NOW());";
	
	private static final String CLEAR_ROSTER_ITEMS = "DELETE FROM roster WHERE \"owner\" = ?;";
	
	private static final String GET_ROSTER_GROUPS = "SELECT DISTINCT(\"group\") FROM roster WHERE \"owner\" = ?";

	private static final String GET_ROSTER_GROUP = "SELECT \"group\" FROM roster WHERE \"owner\" = ? and \"user\" = ?";

	private static final String ADD_ROSTER_ENTRY = "INSERT INTO roster VALUES (?, ?, ?);";
	
	@Override
	public String selectOwner() {
		return GET_OWNER;
	}
	
	@Override
	public String deleteOwner() {
		return DELETE_OWNER;
	}
	
	@Override
	public String addOwner() {
		return ADD_OWNER;
	}

	@Override
	public String clearRoster() {
		return CLEAR_ROSTER_ITEMS;
	}

	@Override
	public String getRosterGroups() {
		return GET_ROSTER_GROUPS;
	}

	@Override
	public String addRosterEntry() {
		return ADD_ROSTER_ENTRY;
	}

	@Override
	public String getRosterGroup() {
		return GET_ROSTER_GROUP;
	}
}